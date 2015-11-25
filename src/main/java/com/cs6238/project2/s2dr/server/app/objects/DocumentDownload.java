package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class DocumentDownload {

    public static class Builder {
        private String documentName;
        private InputStream contents;

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
                    documentName,
                    contents);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String documentName;
    private final InputStream contents;

    private DocumentDownload(
            String documentName,
            InputStream contents) {

        this.documentName = requireNonNull(documentName);
        this.contents = requireNonNull(contents);
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
