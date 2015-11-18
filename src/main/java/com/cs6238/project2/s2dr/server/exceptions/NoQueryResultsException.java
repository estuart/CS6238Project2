package com.cs6238.project2.s2dr.server.exceptions;

public class NoQueryResultsException extends UnexpectedQueryResultsException {
    public NoQueryResultsException(String message) {
        super(message);
    }
}
