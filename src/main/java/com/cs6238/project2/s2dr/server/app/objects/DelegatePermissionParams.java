package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Optional;

public class DelegatePermissionParams {

    private DocumentPermission permission;
    private int userId;
    private Long timeLimitMillis;
    private boolean canPropogate;

    @SuppressWarnings("unused")
    // this default is required for Jackson
    public DelegatePermissionParams() {}

    public DelegatePermissionParams(
            DocumentPermission permission,
            int userId,
            Long timeLimitMillis,
            boolean canPropogate) {

        this.permission = permission;
        this.userId = userId;
        this.timeLimitMillis = timeLimitMillis;
        this.canPropogate = canPropogate;
    }

    public DocumentPermission getPermission() {
        return permission;
    }

    public int getUserId() {
        return userId;
    }

    public Optional<Long> getTimeLimitMillis() {
        return Optional.ofNullable(timeLimitMillis);
    }

    public boolean getCanPropogate() {
        return canPropogate;
    }

    public static DelegatePermissionParams getUploaderPermissions(int userId) {
        return new DelegatePermissionParams(
                DocumentPermission.OWNER,
                userId,
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
