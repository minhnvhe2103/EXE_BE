package com.exe.super_rice.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNAUTHENTICATED(1006, "Wrong username or password",HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1007, "Product not found", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_REQUEST(1008, "Invalid product request", HttpStatus.BAD_REQUEST),
    PRODUCT_CREATION_FAILED(1009, "Failed to create product", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_UPDATE_FAILED(1010, "Failed to update product", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_DELETE_FAILED(1011, "Failed to delete product", HttpStatus.INTERNAL_SERVER_ERROR),

    CART_NOT_FOUND(1012, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(1013, "Cart item not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(1014, "Insufficient stock available", HttpStatus.BAD_REQUEST),
    CART_IS_EMPTY(1015, "Cart is empty", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_CART_ACCESS(1016, "Unauthorized access to cart", HttpStatus.FORBIDDEN),
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
