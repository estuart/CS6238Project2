package com.cs6238.project2.s2dr.server.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class DelegatePermissionParams {

    private DocumentPermission permission;
    private String clientId;
    private boolean canPropogate;

    public DocumentPermission getPermission() {
        return permission;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean getCanPropogate() {
        return canPropogate;
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
