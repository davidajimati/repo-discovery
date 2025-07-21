package com.ajimati.MicroServiceDiscoveryService.exception;

import com.ajimati.MicroServiceDiscoveryService.enums.Responses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomRuntimeException extends RuntimeException {
    private String responseCode;
    private String responseMsg;
    private Object responseDetails;
    private HttpStatus httpStatus;

    public CustomRuntimeException(Responses responseEnum) {
        responseCode = responseEnum.getResponseCode();
        responseMsg = responseEnum.getResponseMsg();
        httpStatus = responseEnum.getHttpStatus();
    }
}
