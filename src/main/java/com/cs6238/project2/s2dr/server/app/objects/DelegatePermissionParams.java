package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;

public class DelegatePermissionParams {

    private DocumentPermission permission;
    private String userName;
    private Long timeLimitMillis;
    private boolean canPropogate;

    @SuppressWarnings("unused")
    // this default is required for Jackson
    public DelegatePermissionParams() {}

    public DelegatePermissionParams(
            DocumentPermission permission,
            String userName,
            Long timeLimitMillis,
            boolean canPropogate) {

        this.permission = permission;
        this.userName = userName;
        this.timeLimitMillis = timeLimitMillis;
        this.canPropogate = canPropogate;
    }

    public DocumentPermission getPermission() {
        return permission;
    }

    public String getUserName() {
        return userName;
    }

    public Optional<Long> getTimeLimitMillis() {
        return Optional.ofNullable(timeLimitMillis);
    }

    public boolean getCanPropogate() {
        return canPropogate;
    }

    public static DelegatePermissionParams getUploaderPermissions(String userName) {
        return new DelegatePermissionParams(
                DocumentPermission.OWNER,
                userName,
                null,
                true);
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
