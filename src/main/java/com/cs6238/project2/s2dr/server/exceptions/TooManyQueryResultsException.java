package com.cs6238.project2.s2dr.server.exceptions;

public class TooManyQueryResultsException extends UnexpectedQueryResultsException {
    public TooManyQueryResultsException(String message) {
        super(message);
    }
}
