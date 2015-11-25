package com.cs6238.project2.s2dr.server.config.authentication;

import org.apache.shiro.authc.AuthenticationToken;

import javax.security.auth.x500.X500Principal;

// TODO provide access to the principal CN that can be used as the username
public class X509Token implements AuthenticationToken {

    private X500Principal principal;
    private byte[] signature;

    public X509Token(X500Principal principal, byte[] signature) {
        this.principal = principal;
        this.signature = signature;
    }

    @Override
    public Object getPrincipal() {
        return getX500Principal();
    }

    @Override
    public Object getCredentials() {
        return getSignature();
    }

    public byte[] getSignature() {
        return signature;
    }

    public X500Principal getX500Principal() {
        return principal;
    }
}
