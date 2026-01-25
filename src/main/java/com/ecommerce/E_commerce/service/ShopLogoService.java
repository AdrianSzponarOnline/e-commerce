package com.ecommerce.E_commerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface ShopLogoService {
    /**
     * Uploads a new shop logo and updates the logo_url setting.
     * If a logo already exists, it will be replaced.
     * 
     * @param file The logo file to upload
     * @return The URL path to the uploaded logo (e.g., "/uploads/shop/logo.png")
     */
    String uploadLogo(MultipartFile file);
    
    /**
     * Deletes the current shop logo and removes the logo_url setting.
     */
    void deleteLogo();
}
