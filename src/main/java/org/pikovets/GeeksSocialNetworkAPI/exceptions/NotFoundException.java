package org.pikovets.GeeksSocialNetworkAPI.exceptions;

import java.util.function.Supplier;

public class NotFoundException extends RuntimeException implements Supplier<NotFoundException> {
    public NotFoundException(String msg) {
        super(msg);
    }

    @Override
    public NotFoundException get() {
        return this;
    }
}