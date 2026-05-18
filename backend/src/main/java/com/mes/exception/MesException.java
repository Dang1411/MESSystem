package com.mes.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception tùy chỉnh cho nghiệp vụ MES
 */
public class MesException extends RuntimeException {

    private final HttpStatus httpStatus;

    public MesException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public MesException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
