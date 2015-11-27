package com.cs6238.project2.s2dr.server.app.exceptions;

public class UserLacksPermissionException extends Exception {
    public UserLacksPermissionException(String message) {
        super(message);
    }
}
