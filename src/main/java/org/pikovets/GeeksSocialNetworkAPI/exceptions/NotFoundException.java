package org.pikovets.GeeksSocialNetworkAPI.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
}