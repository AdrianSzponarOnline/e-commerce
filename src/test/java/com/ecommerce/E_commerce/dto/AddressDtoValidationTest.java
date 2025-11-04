package com.ecommerce.E_commerce.dto;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AddressDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createDTO_ShouldPass_WhenValid() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "Warsaw", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void createDTO_ShouldFail_WhenUserIdIsNull() {
        AddressCreateDTO dto = new AddressCreateDTO(
                null, "123 Main St", null, "Warsaw", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("User ID is required");
    }

    @Test
    void createDTO_ShouldFail_WhenLine1IsBlank() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "", null, "Warsaw", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Address line 1"))).isTrue();
    }

    @Test
    void createDTO_ShouldFail_WhenCityIsBlank() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("City"))).isTrue();
    }

    @Test
    void createDTO_ShouldFail_WhenPostalCodeIsBlank() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "Warsaw", null, "", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Postal code"))).isTrue();
    }

    @Test
    void createDTO_ShouldFail_WhenCountryIsBlank() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "123 Main St", null, "Warsaw", null, "00-001", "", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("Country"))).isTrue();
    }

    @Test
    void createDTO_ShouldFail_WhenContainsHtml() {
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, "<script>alert('xss')</script>", null, "Warsaw", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("HTML"))).isTrue();
    }

    @Test
    void createDTO_ShouldFail_WhenExceedsMaxLength() {
        String longString = "A".repeat(256);
        AddressCreateDTO dto = new AddressCreateDTO(
                1L, longString, null, "Warsaw", null, "00-001", "Poland", true
        );

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("255"))).isTrue();
    }

    @Test
    void updateDTO_ShouldPass_WhenValid() {
        AddressUpdateDTO dto = new AddressUpdateDTO(
                "456 New St", null, "Krakow", null, "30-001", "Poland", false
        );

        Set<ConstraintViolation<AddressUpdateDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void updateDTO_ShouldPass_WhenAllFieldsNull() {
        AddressUpdateDTO dto = new AddressUpdateDTO(null, null, null, null, null, null, null);

        Set<ConstraintViolation<AddressUpdateDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty(); // All fields are optional in update
    }
}

