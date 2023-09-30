package org.pikovets.GeeksSocialNetworkAPI.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) {
        super(msg);
    }
}