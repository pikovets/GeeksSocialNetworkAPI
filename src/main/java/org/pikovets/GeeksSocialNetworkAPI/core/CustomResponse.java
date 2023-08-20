package org.pikovets.GeeksSocialNetworkAPI.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CustomResponse<T> {
    private final int code;

    private  String message;

    private final Collection<T> responseList;

    public CustomResponse(Collection<T> response, CustomStatus customStatus) {
        this.code = customStatus.getCode();
        this.message = customStatus.getMessage();
        this.responseList = response;
    }
}
