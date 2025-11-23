package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.RegisterRequestDTO;
import com.ecommerce.E_commerce.dto.auth.UserDto;
import com.ecommerce.E_commerce.dto.auth.UserUpdateDTO;
import com.ecommerce.E_commerce.exception.EmailAlreadyExistsException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.exception.RoleNotFountException;
import com.ecommerce.E_commerce.model.ERole;
import com.ecommerce.E_commerce.model.Role;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.RoleRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User details not found for the user " + username));

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        return user;
    }

    @Transactional
    public UserDto createUser(RegisterRequestDTO request) {
        checkEmail(request.email());
        Role role = roleRepository.findByRole(ERole.ROLE_USER).orElseThrow(() -> new RoleNotFountException("ROLE_USER not found"));
        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));
        user.setEnabled(true); // Explicitly set user as active
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    private UserDto mapToDto(User user) {
        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream()
                        .map(role -> role.getRole().name())
                        .collect(Collectors.toSet())
                : Set.of();
        
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleNames
        );
    }

    private void checkEmail(String email) throws EmailAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    @Transactional(readOnly = true)
    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        
        // Security check: User can only access their own data (unless OWNER)
        checkUserAccess(user);
        
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
        
        // Security check: User can only access their own data (unless OWNER)
        checkUserAccess(user);
        
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        
        // Security check: User can only update their own profile (unless OWNER)
        checkUserAccess(user);
        
        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
        
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        
        // Security check: User can only delete their own account (unless OWNER)
        checkUserAccess(user);
        
        // Force logout / invalidate session (business logic)
        user.setEnabled(false);
        
        // Soft Delete through JPA (@SQLDelete in entity will set deleted_at and is_active = false)
        userRepository.delete(user);
    }

    // --- SECURITY & HELPER METHODS ---
    
    private void checkUserAccess(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER"));
        
        if (!isOwner) {
            String currentUserEmail = authentication.getName();
            if (user.getEmail() == null || !user.getEmail().equals(currentUserEmail)) {
                throw new AccessDeniedException("You can only access your own user data");
            }
        }
    }
}
