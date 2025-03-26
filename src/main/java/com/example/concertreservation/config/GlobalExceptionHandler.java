package com.example.concertreservation.config;

import com.example.concertreservation.common.exception.InvalidRequestException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> invalidRequestExceptionException(InvalidRequestException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return getErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return getErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().stream()
                    .findFirst()
                    .map(JsonMappingException.Reference::getFieldName)
                    .orElse("알 수 없는 필드");

            String invalidValue = String.valueOf(invalidFormatException.getValue());
            String errorMessage = String.format("필드 '%s'에 대한 값 '%s'이(가) 올바르지 않습니다.", fieldName, invalidValue);


            return getErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return getErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, status);
    }
}
