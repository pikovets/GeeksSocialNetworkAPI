package org.pikovets.GeeksSocialNetworkAPI.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}