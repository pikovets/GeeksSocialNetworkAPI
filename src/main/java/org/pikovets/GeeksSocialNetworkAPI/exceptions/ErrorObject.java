package org.pikovets.GeeksSocialNetworkAPI.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorObject {
    private Integer statusCode;
    private String message;
    private Date timestamp;
}