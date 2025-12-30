package com.ecommerce.E_commerce.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonAccessDeniedHandler.class);
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        logger.warn("Access denied for request: {} {} - {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"timestamp\":\"" + OffsetDateTime.now() + "\"," +
                "\"status\":403,\"error\":\"Access Denied\",\"message\":\"You don't have permission to access this resource\"}";
        response.getWriter().write(body);
    }
}
