package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER'))")
    public ResponseEntity<AddressDTO> create(
            @Valid @RequestBody AddressCreateDTO dto,
            @AuthenticationPrincipal User user) {
        AddressCreateDTO dtoWithUserId = new AddressCreateDTO(
                user.getId(),
                dto.line1(),
                dto.line2(),
                dto.city(),
                dto.region(),
                dto.postalCode(),
                dto.country(),
                dto.isActive()
        );
        AddressDTO address = addressService.create(dtoWithUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressServiceImpl.isAddressOwner(#id, authentication.name))")
    public ResponseEntity<AddressDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressUpdateDTO dto) {
        AddressDTO address = addressService.update(id, dto);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressServiceImpl.isAddressOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressServiceImpl.isAddressOwner(#id, authentication.name))")
    public ResponseEntity<AddressDTO> getById(@PathVariable Long id) {
        AddressDTO address = addressService.getById(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<List<AddressDTO>> getByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<List<AddressDTO>> getActiveByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getActiveByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<AddressDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<AddressDTO> addresses = addressService.findAll(
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        sortDir.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending()
                ));
        return ResponseEntity.ok(addresses);
    }
}

