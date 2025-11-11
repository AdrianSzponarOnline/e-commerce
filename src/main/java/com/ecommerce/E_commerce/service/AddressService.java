package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AddressService {

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #dto.userId == authentication.principal.id)")
    AddressDTO create(AddressCreateDTO dto);

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressService.isAddressOwner(#id, authentication.name))")
    AddressDTO update(Long id, AddressUpdateDTO dto);

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressService.isAddressOwner(#id, authentication.name))")
    void delete(Long id);

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and @addressService.isAddressOwner(#id, authentication.name))")
    AddressDTO getById(Long id);

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    List<AddressDTO> getByUserId(Long userId);

    @PreAuthorize("hasRole('OWNER') or (hasRole('USER') and #userId == authentication.principal.id)")
    List<AddressDTO> getActiveByUserId(Long userId);

    @PreAuthorize("hasRole('OWNER')")
    Page<AddressDTO> findAll(Pageable pageable);
    
    // Helper method for security
    boolean isAddressOwner(Long addressId, String userEmail);
}

