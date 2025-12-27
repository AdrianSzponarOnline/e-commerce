package com.ecommerce.E_commerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("Resource not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<Object> handleInvalidOperation(InvalidOperationException e) {
        logger.warn("Invalid operation: {}", e.getMessage());
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
        logger.warn("Invalid request payload: {}", detailedMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", userMessage);
    }

    @ExceptionHandler(RoleNotFountException.class)
    public ResponseEntity<Object> handleRoleNotFountException(RoleNotFountException e) {
        logger.warn("Role not found: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Role not fount", e.getMessage());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage() != null ? e.getMessage() : "You don't have permission to access this resource";
        logger.warn("Access denied: {}", message);
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied", message);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception e) {
        // Logujemy tylko wiadomość, bez stack trace - to błąd biznesowy, nie awaria systemu
        logger.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid username or password");
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Object> handleInsufficientAuthenticationException(InsufficientAuthenticationException e) {
        logger.warn("Insufficient authentication: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Required", "Full authentication is required to access this resource");
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        logger.warn("Email already exists: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Email Already Exists", e.getMessage());
    }

    @ExceptionHandler(SeoSlugAlreadyExistsException.class)
    public ResponseEntity<Object> handleSeoSlugAlreadyExistsException(SeoSlugAlreadyExistsException e) {
        logger.warn("SEO slug already exists: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Slug Already Exists", e.getMessage());
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException e) {
        logger.warn("Duplicate resource: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Resource", e.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStockException(InsufficientStockException e) {
        logger.warn("Insufficient stock: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Insufficient Stock", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred. Please contact support.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex) {
        return new ResponseEntity<>("The requested resource was not found.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) { // <--- MUST be this specific type
        String name = ex.getName();
        Class<?> requiredType = ex.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "unknown";
        Object value = ex.getValue();

        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                name, value, typeName);

        // Return your error object (or a simple string for now)
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
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
