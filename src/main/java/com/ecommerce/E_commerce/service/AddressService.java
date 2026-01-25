package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AddressService {

    AddressDTO create(AddressCreateDTO dto, Long id);

    AddressDTO update(Long id, AddressUpdateDTO dto);

    void delete(Long id);

    AddressDTO getById(Long id);

    List<AddressDTO> getByUserId(Long userId);

    List<AddressDTO> getActiveByUserId(Long userId);

    Page<AddressDTO> findAll(Pageable pageable);
    
    boolean isAddressOwner(Long addressId, String userEmail);
}

