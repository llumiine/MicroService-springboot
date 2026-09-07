package com.formation.loan.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ApiError> handleLoanNotFound(LoanNotFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({NoAvailableCopiesException.class, LoanAlreadyReturnedException.class})
    public ResponseEntity<ApiError> handleConflictExceptions(RuntimeException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiError> handleFeignNotFound(FeignException.NotFound ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Le livre n'existe pas");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<ApiError> handleFeignConflict(FeignException.Conflict ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "Conflict", "Rupture de stock survenue lors de la réservation (concurrence).");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}