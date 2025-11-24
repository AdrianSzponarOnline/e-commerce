package com.ecommerce.E_commerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Pobieramy ścieżkę absolutną do folderu uploads
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // Konwersja do URI zapewnia poprawną obsługę separatorów ścieżek na różnych systemach
        // Dodajemy ukośnik na końcu, aby Spring poprawnie mapował ścieżki
        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        
        // Mapowanie:
        // URL: /uploads/** -> Folder na dysku: file:/sciezka/do/projektu/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}


