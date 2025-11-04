package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.address.AddressCreateDTO;
import com.ecommerce.E_commerce.dto.address.AddressDTO;
import com.ecommerce.E_commerce.dto.address.AddressUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.AddressMapper;
import com.ecommerce.E_commerce.model.Address;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.AddressRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository,
                              UserRepository userRepository,
                              AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }

    @Override
    public AddressDTO create(AddressCreateDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.userId()));

        Address address = addressMapper.toAddress(dto);
        address.setUser(user);
        address.setCreatedAt(Instant.now());
        address.setUpdatedAt(Instant.now());
        address.setIsActive(dto.isActive() != null ? dto.isActive() : true);

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressDTO(savedAddress);
    }

    @Override
    public AddressDTO update(Long id, AddressUpdateDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        addressMapper.updateAddressFromDTO(dto, address);
        address.setUpdatedAt(Instant.now());

        if (dto.isActive() != null) {
            address.setIsActive(dto.isActive());
        }

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressDTO(savedAddress);
    }

    @Override
    public void delete(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        address.setDeletedAt(Instant.now());
        address.setIsActive(false);
        address.setUpdatedAt(Instant.now());

        addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        return addressMapper.toAddressDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getActiveByUserId(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdAndIsActive(userId, true);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> findAll(Pageable pageable) {
        return addressRepository.findAll(pageable)
                .map(addressMapper::toAddressDTO);
    }
}

