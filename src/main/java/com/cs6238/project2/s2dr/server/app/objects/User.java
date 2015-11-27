package com.cs6238.project2.s2dr.server.app.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class User {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userName;

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public User build() {
            return new User(userName);
        }
    }

    private final String userName;

    private User(String userName) {

        this.userName = userName;
    }

    public String getUserName() {
        return userName;
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
