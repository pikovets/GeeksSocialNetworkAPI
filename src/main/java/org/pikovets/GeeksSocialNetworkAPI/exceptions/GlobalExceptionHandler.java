package org.pikovets.GeeksSocialNetworkAPI.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorObject>> handleNotFoundException(NotFoundException ex, ServerWebExchange exchange) {
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimestamp(new Date());

        return Mono.just(new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ErrorObject>> handleBadRequestException(BadRequestException ex, ServerWebExchange exchange) {
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimestamp(new Date());

        return Mono.just(new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public Mono<ResponseEntity<ErrorObject>> handleContentUnauthorized(UnAuthorizedException ex) {
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimestamp(new Date());

        return Mono.just(new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(NotAllowedException.class)
    public Mono<ResponseEntity<ErrorObject>> handleContentNotAllowed(NotAllowedException ex) {
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimestamp(new Date());

        return Mono.just(new ResponseEntity<>(errorObject, HttpStatus.METHOD_NOT_ALLOWED));
    }
}
