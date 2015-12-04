package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.exceptions.DocumentNotFoundException;
import com.cs6238.project2.s2dr.server.app.exceptions.DocumentIntegrityVerificationException;
import com.cs6238.project2.s2dr.server.app.exceptions.NoQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UnexpectedQueryResultsException;
import com.cs6238.project2.s2dr.server.app.exceptions.UserLacksPermissionException;
import com.cs6238.project2.s2dr.server.app.objects.CurrentUser;
import com.cs6238.project2.s2dr.server.app.objects.DelegatePermissionParams;
import com.cs6238.project2.s2dr.server.app.objects.DocumentDownload;
import com.cs6238.project2.s2dr.server.app.objects.DocumentPermission;
import com.cs6238.project2.s2dr.server.app.objects.EncryptedDocument;
import com.cs6238.project2.s2dr.server.app.objects.SecurityFlag;
import com.google.common.io.Files;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private final CurrentUser currentUser;
    private final DocumentDao documentDao;
    private final EncryptionService encryptionService;

    @Inject
    public DocumentService(
            CurrentUser currentUser,
            DocumentDao documentDao,
            EncryptionService encryptionService) {

        this.currentUser = currentUser;
        this.documentDao = documentDao;
        this.encryptionService = encryptionService;
    }

    public void uploadDocument(File document,
                               String documentName,
                               Set<SecurityFlag> securityFlags,
                               InputStream signature)
            throws SQLException, FileNotFoundException, UnexpectedQueryResultsException, UserLacksPermissionException {

        ByteSource documentContents;
        Optional<byte[]> encryptionKey;
        Optional<byte[]> documentSignature;

        if (securityFlags.contains(SecurityFlag.CONFIDENTIALITY)) {

            // if the upload's security flag is set to `CONFIDENTIALITY`, then the file has to be encrypted
            // before it is written to the database

            LOG.info("CONFIDENTIALITY was selected for the document \"{}\" check-in. Encrypting the file before write",
                    documentName);

            EncryptedDocument encryptedDocument = encryptionService.encryptDocument(document);

            // use the encrypted contents and the encrypted encryption key for write
            documentContents = encryptedDocument.getEncryptedDocument();
            encryptionKey = Optional.of(encryptedDocument.getEncryptedAesKey().getBytes());

        } else {

            // `CONFIDENTIALITY` was not selected for the file, so don't encrypt it

            LOG.info("CONFIDENTIALITY was not selected for the document \"{}\" check-in. Writing unencrypted document",
                    documentName);

            try {
                documentContents = ByteSource.Util.bytes(Files.toByteArray(document));
            } catch (IOException e) {
                LOG.error("Unable to read document bytes", e);
                throw new RuntimeException(e);
            }
            encryptionKey = Optional.empty();
        }

        if (securityFlags.contains(SecurityFlag.INTEGRITY)) {
            documentSignature = Optional.of(ByteSource.Util.bytes(signature).getBytes());
        } else {
            documentSignature = Optional.empty();
        }

        LOG.info("Checking if document \"{}\" already exists", documentName);
        if (!documentDao.documentExists(documentName)) {

            // the document does not already exist, so we add a new document
            LOG.info("Uploading new document \"{}\"", documentName);
            documentDao.uploadDocument(documentName, documentContents, encryptionKey, documentSignature);

        } else {
            LOG.info("Document \"{}\" already exists", documentName);

            // because the document already exists, we must check if the current user has WRITE permission
            // before allowing them to overwrite the document.
            LOG.info("Checking if user \"{}\" has proper permission to over-write document \"{}\"",
                    currentUser.getUserName(), documentName);

            if (!hasWritePermission(documentDao.getDocPermsForCurrentUser(documentName))) {
                // the user does not have WRITE permission (or a dominating permission) so we throw an exception
                // to prevent them from over-writing

                LOG.info("User \"{}\" lacks WRITE permission for document \"{}\"",
                        currentUser.getUserName(), documentName);

                throw new UserLacksPermissionException(
                        "You must have the correct permission before writing to an existing file");
            }

            // since the exception wasn't thrown above, the user has permission to WRITE
            LOG.info("Overwriting document");
            documentDao.overwriteDocument(documentName, documentContents, encryptionKey, documentSignature);

            // clear out the existing security options for the document. They will be re-set with the new
            // values below
            LOG.info("Removing current security flags");
            documentDao.clearDocumentSecurity(documentName);
        }

        // add any security flags for the document
        for (SecurityFlag securityFlag: securityFlags) {
            LOG.info("Adding SecurityFlag \"{}\" to document \"{}\"", securityFlag, documentName);
            documentDao.setDocumentSecurity(documentName, securityFlag);
        }

        // lastly, when a user uploads a new document, we add an "Owner" permission for that user.
        LOG.info("Adding owner permission to document \"{}\" for user \"{}\"", documentName, currentUser.getUserName());
        documentDao.delegatePermissions(
                documentName,
                DelegatePermissionParams.getUploaderPermissions(currentUser.getUserName()),
                Optional.empty());

    }

    public DocumentDownload downloadDocument(String documentName) throws
            SQLException,
            DocumentNotFoundException,
            UnexpectedQueryResultsException,
            UserLacksPermissionException,
            DocumentIntegrityVerificationException {

        LOG.info("Checking if user \"{}\" has proper permission to check-out document \"{}\"",
                currentUser.getUserName(), documentName);

        // if the user doesn't have read permission, then we throw an exception
        if (!hasReadPermission(documentDao.getDocPermsForCurrentUser(documentName))) {
            LOG.info("User \"{}\" lacks READ permission for document \"{}\"", currentUser.getUserName(), documentName);
            throw new UserLacksPermissionException("You must have the correct permission before checking-out a file");
        }

        DocumentDownload unalteredDownload = documentDao.downloadDocument(documentName);

        // see if any security settings were configured for the document
        LOG.info("Checking if any security flags are configured for \"{}\"", documentName);
        EnumSet<SecurityFlag> documentSecurity = documentDao.getDocumentSecurity(documentName);
        LOG.info("Found security flags {}", documentSecurity);

        DocumentDownload returnValue; // where the eventual document return value will be held

        if (documentSecurity.contains(SecurityFlag.CONFIDENTIALITY)) {

            // since CONFIDENTIALITY was chosen, me must first decrypt the file before returning it to the users

            ByteSource encryptionKey = ByteSource.Util.bytes(unalteredDownload.getEncryptionKey().get());
            ByteSource encryptedContents = ByteSource.Util.bytes(unalteredDownload.getContents());

            EncryptedDocument document = new EncryptedDocument(
                    encryptionKey,
                    encryptedContents);

            LOG.info("CONFIDENTIALITY was chosen for file \"{}\". Decrypting document before check-out", documentName);
            ByteSource decryptedDocument = encryptionService.decryptDocument(document);

            returnValue = DocumentDownload.builder()
                    .setDocumentName(unalteredDownload.getDocumentName())
                    .setUploadUserName(unalteredDownload.getUploadUserName())
                    .setContents(new ByteArrayInputStream(decryptedDocument.getBytes()))
                    // no need to set encryptionKey at this point
                    .setSignature(unalteredDownload.getSignature())
                    .build();
        } else {

            // since CONFIDENTIALITY was not chosen, we return the unaltered document
            returnValue = unalteredDownload;
        }

        if (documentSecurity.contains(SecurityFlag.INTEGRITY)) {

            LOG.info("INTEGRITY was chosen for file \"{}\". Will verify document using public key of uploader",
                    documentName);

            LOG.info("Fetching public key of document uploader \"{}\"", returnValue.getUploadUserName());
            RSAPublicKeySpec uploaderKeySpec
                    = documentDao.getUserPubKeySpec(returnValue.getUploadUserName());

            ByteSource contentsToVerify = ByteSource.Util.bytes(returnValue.getContents());

            // `getSignature().get()` will throw a runtime exception if the signature is not present, but it should
            // *always* be present if the INTEGRITY flag is set, therefore we will let it explode
            ByteSource documentSignature = ByteSource.Util.bytes(returnValue.getSignature().get());

            if (!encryptionService.canVerifySignature(contentsToVerify, documentSignature, uploaderKeySpec)) {
                LOG.info("Could not verify the document's signature");
                throw new DocumentIntegrityVerificationException();
            }

            LOG.info("Successfully verified the signature");
        }

        // if the signature needed to be verified and could not, an exception would have been thrown above.
        // at this point we can confidently return the valid document
        LOG.info("User \"{}\" checking-out document \"{}\"", currentUser.getUserName(), documentName);
        return returnValue;
    }

    public void delegatePermissions(String documentName, DelegatePermissionParams delegateParams)
            throws SQLException, UserLacksPermissionException, NoQueryResultsException {

        LOG.info("Checking if user \"{}\" can delegate permission \"{}\" to file \"{}\"",
                currentUser.getUserName(), delegateParams.getPermission(), documentName);

        // build the set of "valid permission" able to propagate the permission further
        // for instance having the BOTH permission allows a user to propagate READ
        EnumSet<DocumentPermission> validPermissions;
        switch (delegateParams.getPermission()) {
            case READ:
                validPermissions = EnumSet.of(
                        DocumentPermission.READ,
                        DocumentPermission.BOTH,
                        DocumentPermission.OWNER);
                break;
            case WRITE:
                validPermissions = EnumSet.of(
                        DocumentPermission.WRITE,
                        DocumentPermission.BOTH,
                        DocumentPermission.OWNER);
                break;
            case BOTH:
                validPermissions = EnumSet.of(
                        DocumentPermission.BOTH,
                        DocumentPermission.OWNER);
                break;
            case OWNER:
                validPermissions = EnumSet.of(DocumentPermission.OWNER);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Was not expecting DocumentPermission %s", delegateParams.getPermission()));
        }

        if (!documentDao.userCanDelegate(documentName, validPermissions)) {
            // if the user doesn't have the correct permission, *or* doesn't have the ability to propagate it
            // further, then we don't allow them to go any further.
            LOG.info("User \"{}\" cannot propagate \"{}\" further for document \"{}\".",
                    currentUser.getUserName(), delegateParams.getPermission(), documentName);

            throw new UserLacksPermissionException(
                    "You do not possess the ability to further propogate the permission");
        }

        LOG.info("\"{}\" delegating \"{}\" to user \"{}\" for document \"{}\"",
                currentUser.getUserName(), delegateParams.getPermission(), delegateParams.getUserName(), documentName);

        Optional<Long> timeLimit = documentDao.getMaxDelegationTime(documentName, validPermissions);

        documentDao.delegatePermissions(documentName, delegateParams, timeLimit);
    }

    public void deleteDocument(String documentName)
            throws SQLException, NoQueryResultsException, UserLacksPermissionException {

        LOG.info("Checking if user \"{}\" has proper permission to delete document \"{}\"",
                currentUser.getUserName(), documentName);

        if (!hasWritePermission(documentDao.getDocPermsForCurrentUser(documentName))) {
            LOG.info("User \"{}\" must have a valid OWNER permission to delete a document", currentUser.getUserName());
            throw new UserLacksPermissionException("Only a document's owner is allowed to delete a file.");
        }

        // delete all permissions for the document before deleting the document
        LOG.info("Deleting all permissions for document \"{}\"", documentName);
        documentDao.deleteAllDocumentPermissions(documentName);

        LOG.info("Removing all security flags for document \"{}\"", documentName);
        documentDao.clearDocumentSecurity(documentName);

        LOG.info("Performing safe delete on document \"{}\"", documentName);
        documentDao.deleteDocument(documentName);

        LOG.info("Successfully deleted document \"{}\"", documentName);
    }

    private boolean hasReadPermission(Set<DocumentPermission> permissions) {
        return permissions.contains(DocumentPermission.READ)
                || permissions.contains(DocumentPermission.BOTH)
                || permissions.contains(DocumentPermission.OWNER);
    }

    private boolean hasWritePermission(Set<DocumentPermission> permissions) {
        return permissions.contains(DocumentPermission.WRITE)
                || permissions.contains(DocumentPermission.BOTH)
                || permissions.contains(DocumentPermission.OWNER);
    }
}
