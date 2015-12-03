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
        private InputStream contents;
        private Optional<byte[]> encryptionKey = Optional.empty();

        public Builder setDocumentName(String documentName) {
            this.documentName = documentName;
            return this;
        }

        public Builder setContents(InputStream contents) {
            this.contents = contents;
            return this;
        }

        public Builder setEncryptionKey(Optional<byte[]> encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }

        public DocumentDownload build() {
            return new DocumentDownload(
                    documentName,
                    contents,
                    encryptionKey);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String documentName;
    private final InputStream contents;
    private final Optional<byte[]> encryptionKey;

    private DocumentDownload(
            String documentName,
            InputStream contents,
            Optional<byte[]> encryptionKey) {

        this.documentName = requireNonNull(documentName);
        this.contents = requireNonNull(contents);
        this.encryptionKey = encryptionKey;
    }

    public String getDocumentName() {
        return documentName;
    }

    public InputStream getContents() {
        return contents;
    }

    public Optional<byte[]> getEncryptionKey() {
        return encryptionKey;
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
