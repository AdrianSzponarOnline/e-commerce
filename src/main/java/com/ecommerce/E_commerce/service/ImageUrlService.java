package com.ecommerce.E_commerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageUrlService {

    @Value("${app.image-base-url:http://localhost:8080}")
    private String imageBaseUrl;

    public String buildFullUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) {
            return relativeUrl;
        }
        
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        
        if (relativeUrl.startsWith("/")) {
            String base = imageBaseUrl.endsWith("/") 
                    ? imageBaseUrl.substring(0, imageBaseUrl.length() - 1) 
                    : imageBaseUrl;
            return base + relativeUrl;
        }
        
        String base = imageBaseUrl.endsWith("/") ? imageBaseUrl : imageBaseUrl + "/";
        return base + relativeUrl;
    }
}

