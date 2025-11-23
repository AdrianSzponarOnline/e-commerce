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

import java.util.List;
import java.util.Optional;

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

    /**
     * Helper method to check if current user is OWNER
     * @return true if user has ROLE_OWNER, false otherwise
     */
    private boolean isOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_OWNER"));
    }

    /**
     * Helper method to get current authenticated user
     * @return Optional containing User if found, empty otherwise
     */
    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail);
    }

    /**
     * Helper method to check if user has access to resource
     * @param resourceUserId ID of the user who owns the resource
     * @param errorMessage Error message to throw if access denied
     * @throws AccessDeniedException if user doesn't have access
     */
    private void checkAccess(Long resourceUserId, String errorMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!isOwner()) {
            User currentUser = getCurrentUser()
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            if (!currentUser.getId().equals(resourceUserId)) {
                throw new AccessDeniedException(errorMessage);
            }
        }
    }

    @Override
    public AddressDTO create(AddressCreateDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.userId()));

        checkAccess(dto.userId(), "You can only create addresses for yourself");


        Address address = addressMapper.toAddress(dto);
        address.setUser(user);
        address.setIsActive(dto.isActive() != null ? dto.isActive() : true);
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressDTO(savedAddress);
    }

    @Override
    public AddressDTO update(Long id, AddressUpdateDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        if (address.getUser() == null) {
            throw new ResourceNotFoundException("Address has no associated user");
        }
        checkAccess(address.getUser().getId(), "You can only update your own addresses");

        addressMapper.updateAddressFromDTO(dto, address);

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
        
        if (address.getUser() == null) {
            throw new ResourceNotFoundException("Address has no associated user");
        }
        checkAccess(address.getUser().getId(), "You can only delete your own addresses");

        addressRepository.delete(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDTO getById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        if (address.getUser() == null) {
            throw new ResourceNotFoundException("Address has no associated user");
        }
        checkAccess(address.getUser().getId(), "You can only view your own addresses");
        
        return addressMapper.toAddressDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getByUserId(Long userId) {
        checkAccess(userId, "You can only view your own addresses");
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getActiveByUserId(Long userId) {
        checkAccess(userId, "You can only view your own addresses");
        
        List<Address> addresses = addressRepository.findByUserIdAndIsActive(userId, true);
        return addresses.stream()
                .map(addressMapper::toAddressDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressDTO> findAll(Pageable pageable) {
    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        if (!isOwner()) {
            throw new AccessDeniedException("Only OWNER can view all addresses");
        }
        
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

