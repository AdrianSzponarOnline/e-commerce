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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // Security check: User can only create address for themselves (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
            
            if (!currentUser.getId().equals(dto.userId())) {
                throw new AccessDeniedException("You can only create addresses for yourself");
            }
        }
        
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
        
        // Security check: User can only update their own addresses (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            if (address.getUser() == null || !address.getUser().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You can only update your own addresses");
            }
        }

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
        
        // Security check: User can only delete their own addresses (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            if (address.getUser() == null || !address.getUser().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You can only delete your own addresses");
            }
        }

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
        
        // Security check: User can only view their own addresses (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            if (address.getUser() == null || !address.getUser().getEmail().equals(userEmail)) {
                throw new AccessDeniedException("You can only view your own addresses");
            }
        }
        
        return addressMapper.toAddressDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getByUserId(Long userId) {
        // Security check: User can only view their own addresses (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
            
            if (!currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("You can only view your own addresses");
            }
        }
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getActiveByUserId(Long userId) {
        // Security check: User can only view their own addresses (unless OWNER)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
            
            if (!currentUser.getId().equals(userId)) {
                throw new AccessDeniedException("You can only view your own addresses");
            }
        }
        
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
    
    @Override
    public boolean isAddressOwner(Long addressId, String userEmail) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        return address.getUser() != null && address.getUser().getEmail().equals(userEmail);
    }
}

