package com.example.carins.web;

import com.example.carins.service.CarNotFoundException;
import com.example.carins.service.PolicyOverlapException;
import com.example.carins.service.PolicyValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Validation failed");
        body.put("message", msg);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Malformed JSON or wrong data types (check date format: yyyy-MM-dd)");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Data integrity violation (likely a required field is missing or invalid). Check endDate and date range.");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<?> handleTx(org.springframework.transaction.TransactionSystemException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Validation failed while saving entity. Ensure endDate is provided and endDate >= startDate.");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(jakarta.validation.ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Validation failed");
        body.put("message", msg);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<?> handleCarNotFound(CarNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", 404, "error", "Not Found", "message", ex.getMessage()));
    }

    @ExceptionHandler({ DateTimeParseException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<?> handleBadDate(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Invalid 'date' format. Expected yyyy-MM-dd"
                ));
    }

    @ExceptionHandler(PolicyValidationException.class)
    public ResponseEntity<?> handlePolicyValidation(PolicyValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(PolicyOverlapException.class)
    public ResponseEntity<?> handlePolicyOverlap(PolicyOverlapException ex) {
        return ResponseEntity.status(409).body(Map.of(
                "status", 409,
                "error", "Conflict",
                "message", ex.getMessage()
        ));
    }
}