package com.cs6238.project2.s2dr.server.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class DocumentDownload {

    public static class Builder {
        private int documentId;
        private String documentName;
        private InputStream contents;

        public Builder setDocumentId(int documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder setDocumentName(String documentName) {
            this.documentName = documentName;
            return this;
        }

        public Builder setContents(InputStream contents) {
            this.contents = contents;
            return this;
        }

        public DocumentDownload build() {
            return new DocumentDownload(
                    documentId,
                    documentName,
                    contents);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final int documentId;
    private final String documentName;
    private final InputStream contents;

    private DocumentDownload(
            int documentId,
            String documentName,
            InputStream contents) {

        this.documentId = requireNonNull(documentId);
        this.documentName = requireNonNull(documentName);
        this.contents = requireNonNull(contents);
    }

    public int getDocumentId() {
        return documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public InputStream getContents() {
        return contents;
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
