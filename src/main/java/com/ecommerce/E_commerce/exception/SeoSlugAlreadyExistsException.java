package com.ecommerce.E_commerce.exception;

public class SeoSlugAlreadyExistsException extends RuntimeException {
    public SeoSlugAlreadyExistsException(String seoSlug) {
        super("Category seoSlug already exists: " + seoSlug);
    }
}


