package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.RegisterRequestDTO;
import com.ecommerce.E_commerce.dto.auth.UserDto;
import com.ecommerce.E_commerce.dto.auth.UserUpdateDTO;
import com.ecommerce.E_commerce.exception.EmailAlreadyExistsException;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.exception.RoleNotFountException;
import com.ecommerce.E_commerce.model.ConfirmationToken;
import com.ecommerce.E_commerce.model.ERole;
import com.ecommerce.E_commerce.model.Role;
import com.ecommerce.E_commerce.model.User;
import com.ecommerce.E_commerce.repository.ConfirmationTokenRepository;
import com.ecommerce.E_commerce.repository.RoleRepository;
import com.ecommerce.E_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ConfirmationTokenRepository confirmationTokenRepository;



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
        logger.info("Creating new user: email={}", request.email());
        checkEmail(request.email());
        Role role = roleRepository.findByRole(ERole.ROLE_USER).orElseThrow(() -> new RoleNotFountException("ROLE_USER not found"));
        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));

        user.setEnabled(false);

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        String token = generateAndSaveToken(savedUser, 15);
        String link = "http://localhost:5173/activate?token=" + token;

        logger.debug("Sending activation email: userId={}, email={}", savedUser.getId(), request.email());
        emailService.sendSimpleMail(
                request.email(),
                "Potwierdź swoje konto",
                "Witaj " + request.firstName() + ",\n\n" +
                        "Kliknij w poniższy link, aby aktywować konto:\n" + link
        );

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
        checkUserAccess(user);
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

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
        
        checkUserAccess(user);
        
        user.setEnabled(false);
        
        userRepository.delete(user);
    }


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
    @Transactional
    public void activateAccount(String token) {
        logger.info("Activating account with token");
        ConfirmationToken confirmationToken = getAndValidateToken(token);

        if (confirmationToken.getConfirmedAt() != null) {
            logger.warn("Attempted to activate already activated account");
            throw new IllegalStateException("Email has already been activated");
        }

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        logger.info("Account activated successfully: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Transactional
    public void forgotPassword(String email) {
        logger.info("Processing forgot password request: email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email not found."));

        String token = generateAndSaveToken(user, 30);

        String link = "http://localhost:5173/reset-password?token=" + token;

        logger.debug("Sending password reset email: userId={}, email={}", user.getId(), email);
        emailService.sendSimpleMail(
                email,
                "Resetowanie hasła",
                "Cześć " + user.getFirstName() + ",\n\n" +
                        "Otrzymaliśmy prośbę o zmianę hasła. Kliknij link poniżej:\n" + link + "\n\n" +
                        "Jeśli to nie Ty, zignoruj tę wiadomość."
        );
        logger.info("Password reset email sent: email={}", email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        logger.info("Processing password reset with token");
        ConfirmationToken confirmToken = getAndValidateToken(token);


        if (confirmToken.getConfirmedAt() != null) {
            logger.warn("Attempted to use already used reset token");
            throw new IllegalStateException("Ten link resetujący został już wykorzystany.");
        }

        User user = confirmToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEnabled(true);

        confirmToken.setConfirmedAt(LocalDateTime.now());
        logger.info("Password reset successfully: userId={}, email={}", user.getId(), user.getEmail());
    }

    private String generateAndSaveToken(User user, int expiryMinutes) {
       ConfirmationToken confirmationToken = new ConfirmationToken(user, expiryMinutes);
       confirmationTokenRepository.save(confirmationToken);
       return confirmationToken.getToken();
    }

    private ConfirmationToken getAndValidateToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }
        return confirmationToken;
    }

    @Transactional
    public void resendActivationLink(String email){
        logger.info("Resending activation link for email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("User with email " + email + " not found."));

        if(user.isEnabled()){
            logger.warn("Account already active for email: {}", email);
            throw new InvalidOperationException("Account is already activated");
        }
        String token = generateAndSaveToken(user, 15);
        String link = "http://localhost:5173/activate?token=" + token;

        logger.debug("Sending new activation mail to: {}", email);

        emailService.sendSimpleMail(
                user.getEmail(),
                "Nowy link aktywacyjny",
                "Witaj, " + user.getFirstName() + ". \n\n" +
                        "Poprosiłeś o nowy link aktywacyjny. Kliknij poniżej, aby aktywować konto: \n" + link + "\n\n" +
                        "Link jest ważny przez 15 minut."
        );
    }
}
