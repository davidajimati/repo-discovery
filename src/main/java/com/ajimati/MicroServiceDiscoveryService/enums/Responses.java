package com.ajimati.MicroServiceDiscoveryService.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Responses {
    FAILURE("100", "request failed. Please try again", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("100", "Something went wrong. Please try again", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("00", "No services match", HttpStatus.OK),
    EXPIRED_TOKEN("100", "Access token expired. Contact admin to update token", HttpStatus.FAILED_DEPENDENCY),
    INVALID_ORG_CONFIG("99", "INVALID ORGANIZATION CREDENTIAL CONFIG", HttpStatus.BAD_REQUEST);

    private String responseCode;
    private String responseMsg;
    private HttpStatus httpStatus;

    Responses(String responseCode, String responseMsg, HttpStatus httpStatus) {
    }
}
