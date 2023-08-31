package org.pikovets.GeeksSocialNetworkAPI.core;

import lombok.Getter;

@Getter
public enum CustomStatus {
    SUCCESS(0, "Success"),
    NOT_FOUND(1, "Not found"),
    BAD_REQUEST(2, "Bad request"),
    EXCEPTION(3, "Exception");

    private final int code;
    private final String message;

    CustomStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}