package com.cs6238.project2.s2dr.server.config.authentication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.shiro.authc.AuthenticationToken;

import javax.security.auth.x500.X500Principal;

public class X509Token implements AuthenticationToken {

    private final X500Principal principal;
    private final byte[] signature;

    public X509Token(X500Principal principal, byte[] signature) {
        this.principal = principal;
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature;
    }

    public X500Principal getX500Principal() {
        return principal;
    }

    public String getSubjectCommonName() {
        // `principal.getName` returns the entire subject section of the cert smashed into
        // a single String
        String subjectName = principal.getName();

        // very naive parsing to get the common name. This would never fly in the "real world" :)
        subjectName = subjectName
                .substring(subjectName.indexOf("CN="), subjectName.indexOf(",OU=")) // parse out "CN=commonName"
                .replaceAll("CN=", ""); // remove the "CN=" part

        return subjectName;
    }

    @Override
    public Object getPrincipal() {
        return getX500Principal();
    }

    @Override
    public Object getCredentials() {
        return getSignature();
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
        return String.format(
                "X509Token=[subjectName=%s]", getSubjectCommonName());
    }
}
