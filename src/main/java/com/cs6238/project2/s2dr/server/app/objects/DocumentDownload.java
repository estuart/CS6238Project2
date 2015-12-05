package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.InputStream;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DocumentDownload {

    public static class Builder {
        private String documentName;
        private String uploadUserName;
        private byte[] contents;
        private Optional<byte[]> encryptionKey = Optional.empty();
        private Optional<byte[]> signature = Optional.empty();

        public Builder setDocumentName(String documentName) {
            this.documentName = documentName;
            return this;
        }

        public Builder setUploadUserName(String uploadUserName) {
            this.uploadUserName = uploadUserName;
            return this;
        }

        public Builder setContents(byte[] contents) {
            this.contents = contents;
            return this;
        }

        public Builder setEncryptionKey(Optional<byte[]> encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }

        public Builder setSignature(Optional<byte[]> signature) {
            this.signature = signature;
            return this;
        }

        public DocumentDownload build() {
            return new DocumentDownload(
                    documentName,
                    uploadUserName,
                    contents,
                    encryptionKey,
                    signature);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String documentName;
    private final String uploadUserName;
    private final byte[] contents;
    private final Optional<byte[]> encryptionKey;
    private final Optional<byte[]> signature;

    private DocumentDownload(
            String documentName,
            String uploadUserName,
            byte[] contents,
            Optional<byte[]> encryptionKey,
            Optional<byte[]> signature) {

        this.documentName = requireNonNull(documentName);
        this.uploadUserName = requireNonNull(uploadUserName);
        this.contents = requireNonNull(contents);
        this.encryptionKey = encryptionKey;
        this.signature = signature;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public byte[] getContents() {
        return contents;
    }

    public Optional<byte[]> getEncryptionKey() {
        return encryptionKey;
    }

    public Optional<byte[]> getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
