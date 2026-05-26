package com.example.demo.attendance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        String errorCode = "INTERNAL_ERROR";
        String errorMessage = message;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (message != null && message.contains(":")) {
            String[] parts = message.split(":", 2);
            errorCode = parts[0];
            errorMessage = parts[1];

            status = switch (errorCode) {
                case "WORKER_NOT_FOUND", "SITE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                case "DUPLICATE_CLOCK_IN", "ALREADY_SETTLED" -> HttpStatus.CONFLICT;
                case "WORKER_INACTIVE", "SITE_INACTIVE", "INVALID_MONTH",
                     "NOT_CLOCKED_IN", "NO_ENTRIES" -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }

        return ResponseEntity.status(status).body(Map.of(
                "error", errorCode,
                "message", errorMessage,
                "timestamp", Instant.now().toString()
        ));
    }
}