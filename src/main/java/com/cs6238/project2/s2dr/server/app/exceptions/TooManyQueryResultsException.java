package com.cs6238.project2.s2dr.server.app.exceptions;

public class TooManyQueryResultsException extends UnexpectedQueryResultsException {
    public TooManyQueryResultsException(String message) {
        super(message);
    }
}
