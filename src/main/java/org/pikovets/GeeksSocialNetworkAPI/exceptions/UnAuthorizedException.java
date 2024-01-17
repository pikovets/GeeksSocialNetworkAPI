package org.pikovets.GeeksSocialNetworkAPI.exceptions;

public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String msg) {
        super(msg);
    }
}