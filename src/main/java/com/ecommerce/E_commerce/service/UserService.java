package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.RegisterRequestDTO;
import com.ecommerce.E_commerce.dto.auth.UserDto;
import com.ecommerce.E_commerce.exception.EmailAlreadyExistsException;
import com.ecommerce.E_commerce.exception.RoleNotFountException;
import com.ecommerce.E_commerce.model.ERole;
import com.ecommerce.E_commerce.model.Role;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.RoleRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("User details not found for the user " + username));

        return user;
    }

    @Transactional
    public UserDto createUser(RegisterRequestDTO request) {
        checkEmail(request.email());
        Role role = roleRepository.findByRole(ERole.ROLE_USER).orElseThrow(() -> new RoleNotFountException("ROLE_USER not found"));
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    private Set<Role> resolveRoles() {
        return Set.of(
                roleRepository.findByRole(ERole.ROLE_USER)
                        .orElseThrow(() -> new RoleNotFountException("ROLE_USER not found"))
        );
    }
    private UserDto mapToDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRole().name())
                .toList();

        return new UserDto(user.getEmail());
    }

    private void checkEmail(String email) throws EmailAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    public User findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not found"));
        return user;
    }

    public User findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        return user;
    }
}
