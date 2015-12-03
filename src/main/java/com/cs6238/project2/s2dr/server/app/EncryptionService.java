package com.cs6238.project2.s2dr.server.app;

import com.cs6238.project2.s2dr.server.app.objects.EncryptedDocument;
import com.cs6238.project2.s2dr.server.app.objects.ServerKeyPair;
import com.google.common.io.Files;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.inject.Inject;
import java.io.File;

public class EncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionService.class);

    private static final String RSA_ALGORITHM = "RSA";
    private static final int AES_KEY_BYTE_SIZE = 16;

    private final AesCipherService aesCipher;
    private final RandomNumberGenerator numberGenerator;
    private final ServerKeyPair serverKeyPair;

    @Inject
    public EncryptionService(ServerKeyPair serverKeyPair) {

        this.aesCipher = new AesCipherService();
        this.numberGenerator = new SecureRandomNumberGenerator();
        this.serverKeyPair = serverKeyPair;
    }

    public EncryptedDocument encryptDocument(File document) {
        try {
            // the document itself will be encrypted with AES using a random key
            ByteSource aesKey = numberGenerator.nextBytes(AES_KEY_BYTE_SIZE);

            // encrypt the document contents using AES symmetric encryption
            LOG.info("Encrypting document using random AES key");
            ByteSource encryptedDocument = aesCipher.encrypt(Files.toByteArray(document), aesKey.getBytes());

            // the AES key then must be encrypted with RSA using the server's public key
            LOG.info("Encrypting AES key using server public key");
            Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
            rsaCipher.init(Cipher.ENCRYPT_MODE, serverKeyPair.getPublicKey());
            ByteSource encryptedAesKey = ByteSource.Util.bytes(rsaCipher.doFinal(aesKey.getBytes()));

            // return the encrypted document and the encrypted AES key
            return new EncryptedDocument(encryptedAesKey, encryptedDocument);
        } catch (Exception e) {
            LOG.error("Error encrypting file", e);
            throw new RuntimeException("Internal Server Error");
        }
    }

    public ByteSource decryptDocument(EncryptedDocument encryptedDocument) {
        try {
            // since the AES key was encrypted using the server's public RSA key,
            // we must decrypt the AES key with RSA using the server's private key
            LOG.info("Decrypting AES key using server private key");
            Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
            rsaCipher.init(Cipher.DECRYPT_MODE, serverKeyPair.getPrivateKey());
            ByteSource decryptedAesKey
                    = ByteSource.Util.bytes(rsaCipher.doFinal(encryptedDocument.getEncryptedAesKey().getBytes()));

            // use the decrypted AES key to decrypt the document contents and return
            LOG.info("Decrypting document using decrypted AES key");
            return aesCipher.decrypt(
                    encryptedDocument.getEncryptedDocument().getBytes(),
                    decryptedAesKey.getBytes());


        } catch (Exception e) {
            LOG.error("Error decrypting file", e);
            throw new RuntimeException("Internal Server Error");
        }
    }
}
