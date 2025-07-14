package com.exe.super_rice.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNAUTHENTICATED(1006, "Wrong username or password",HttpStatus.BAD_REQUEST),
        ;
    private int code;
    private String message;

    public String getMessage() {
        return message;
    }

    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    ErrorCode() {
    }
}
