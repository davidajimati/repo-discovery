package com.ajimati.MicroServiceDiscoveryService.exception;

import com.ajimati.MicroServiceDiscoveryService.models.contract.RedboxResponseContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(CustomRuntimeException.class)
    private ResponseEntity<RedboxResponseContract> handleCustomRuntimeException(CustomRuntimeException e) {
        log.info("customException handled ... \n\n");
        return new ResponseEntity<>(new RedboxResponseContract(e.getResponseCode(), e.getResponseMsg(), e.getResponseDetails()),
                e.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<RedboxResponseContract> handleCustomRuntimeException(Exception e) {
        log.info("Exception thrown: {}", e.getMessage());
        log.info("Exception handled ... \n\n");
        return new ResponseEntity<>(new RedboxResponseContract("100", "error", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
