package com.ecommerce.E_commerce.dto;

import com.ecommerce.E_commerce.dto.category.CategoryCreateDTO;
import com.ecommerce.E_commerce.dto.category.CategoryUpdateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createDTO_rejectsHtml() {
        CategoryCreateDTO dto = new CategoryCreateDTO("<b>name</b>", "ok<bad>", "valid-slug", null, true);
        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createDTO_acceptsValid() {
        CategoryCreateDTO dto = new CategoryCreateDTO("phones", "smartphones", "phones", null, true);
        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateDTO_enforcesSlugFormat() {
        CategoryUpdateDTO dto = new CategoryUpdateDTO("name", "desc", "Bad Slug", null, true);
        Set<ConstraintViolation<CategoryUpdateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}


