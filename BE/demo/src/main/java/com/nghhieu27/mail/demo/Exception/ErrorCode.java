package com.nghhieu27.mail.demo.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ENUM_KEY(9998, "Invalid enum key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "User existed!", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1002, "Username must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1004, "User not existed!", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1005, "unauthenticated!", HttpStatus.UNAUTHORIZED);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}