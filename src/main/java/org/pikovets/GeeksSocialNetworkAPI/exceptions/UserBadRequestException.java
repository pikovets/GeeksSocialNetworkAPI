package org.pikovets.GeeksSocialNetworkAPI.exceptions;

public class UserBadRequestException extends RuntimeException {
    public UserBadRequestException(String msg) {
        super(msg);
    }
}