//package com.polyconnect.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(SbtetServiceException.class)
//    public ResponseEntity<Map<String, Object>> handleSbtetException(SbtetServiceException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", "SBTET result service is temporarily unavailable.");
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
//    }
//
//    @ExceptionHandler(InvalidPinException.class)
//    public ResponseEntity<Map<String, Object>> handleInvalidPin(InvalidPinException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", ex.getMessage());
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//    }
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", ex.getMessage());
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
//    }
//
//    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
//    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Unauthorized access");
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
//    }
//
//    @ExceptionHandler({TenantAccessDeniedException.class, AccessDeniedException.class})
//    public ResponseEntity<Map<String, Object>> handleAccessDenied(Exception ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", "Access denied: You do not have permissions for this department/resource.");
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
//        Map<String, Object> body = new HashMap<>();
//        body.put("success", false);
//        body.put("message", "An unexpected error occurred. Please try again later.");
//        body.put("timestamp", LocalDateTime.now());
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }
//}









package com.polyconnect.exception;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SbtetServiceException.class)
    public ResponseEntity<Map<String, Object>> handleSbtetException(SbtetServiceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "SBTET result service is temporarily unavailable.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPin(InvalidPinException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }




    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Unauthorized access");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({TenantAccessDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleAccessDenied(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Access denied: You do not have permissions for this department/resource.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "An unexpected error occurred. Please try again later.");
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbort(Exception ex) {
        // Client disconnected before response completed — not a real error, ignore or log at DEBUG
        log.debug("Client aborted connection before response was fully written: {}", ex.getMessage());
    }
}