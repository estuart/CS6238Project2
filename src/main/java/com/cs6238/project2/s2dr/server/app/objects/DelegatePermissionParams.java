package com.cs6238.project2.s2dr.server.app.objects;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class DelegatePermissionParams {

    private Set<DocumentPermission> permissions;
    private String userName;
    private Long timeLimitMillis;
    private boolean canPropogate;

    @SuppressWarnings("unused")
    // this default is required for Jackson
    public DelegatePermissionParams() {}

    public DelegatePermissionParams(
            Set<DocumentPermission> permissions,
            String userName,
            Long timeLimitMillis,
            boolean canPropogate) {

        this.permissions = permissions;
        this.userName = userName;
        this.timeLimitMillis = timeLimitMillis;
        this.canPropogate = canPropogate;
    }

    public EnumSet<DocumentPermission> getPermissions() {
        return EnumSet.copyOf(permissions);
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
                ImmutableSet.of(DocumentPermission.OWNER),
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
