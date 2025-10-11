package com.ecommerce.E_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Object> handleInvalidOperation(InvalidOperationException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid operation", e.getMessage());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String detailedMessage = e.getMostSpecificCause().getMessage();

        String userMessage;

        if(detailedMessage.contains("not one of the values accepted for Enum class")){
            userMessage = "Unsupported currency";
        }else{
            userMessage = "Invalid request payload";
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", userMessage);
    }

    @ExceptionHandler(AlreadyTransferredException.class)
    public ResponseEntity<Object> handleAlreadyTransferredException(AlreadyTransferredException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Already transferred", e.getMessage());
    }
    @ExceptionHandler(RoleNotFountException.class)
    public ResponseEntity<Object> handleRoleNotFountException(RoleNotFountException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Role not fount", e.getMessage());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied", "You don't have permission to access this resource");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid username or password");
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Object> handleInsufficientAuthenticationException(InsufficientAuthenticationException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Required", "Full authentication is required to access this resource");
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Email Already Exists", e.getMessage());
    }


    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status",status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

}
