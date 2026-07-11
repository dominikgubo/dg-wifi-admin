package com.example.wifiadmin.controllers;

import com.example.wifiadmin.exception.CpeNotFoundException;
import com.example.wifiadmin.exception.MirrorUnavailableException;
import com.example.wifiadmin.exception.PlatformCommunicationException;
import com.example.wifiadmin.models.api.ErrorBody;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorBody> handleValidation(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "Request is invalid", "VALIDATION_ERROR");
    }

    @ExceptionHandler(CpeNotFoundException.class)
    public ResponseEntity<ErrorBody> handleNotFound(CpeNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "CPE was not found", "CPE_NOT_FOUND");
    }

    @ExceptionHandler(PlatformCommunicationException.class)
    public ResponseEntity<ErrorBody> handlePlatformFailure(PlatformCommunicationException exception) {
        return response(HttpStatus.BAD_GATEWAY, "WiFi platform communication failed", "PLATFORM_ERROR");
    }

    @ExceptionHandler(MirrorUnavailableException.class)
    public ResponseEntity<ErrorBody> handleMirrorFailure(MirrorUnavailableException exception) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "WiFi database mirror is unavailable", "MIRROR_UNAVAILABLE");
    }

    private ResponseEntity<ErrorBody> response(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status).body(new ErrorBody(message, code));
    }
}
