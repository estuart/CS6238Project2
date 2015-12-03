package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.shiro.util.ByteSource;

public class EncryptedDocument {

    private final ByteSource encryptedAesKey;
    private final ByteSource encryptedDocument;

    public EncryptedDocument(
            ByteSource encryptedAesKey,
            ByteSource encryptedDocument) {

        this.encryptedAesKey = encryptedAesKey;
        this.encryptedDocument = encryptedDocument;
    }

    public ByteSource getEncryptedAesKey() {
        return encryptedAesKey;
    }

    public ByteSource getEncryptedDocument() {
        return encryptedDocument;
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
