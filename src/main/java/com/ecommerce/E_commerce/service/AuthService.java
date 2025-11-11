package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.auth.AuthRequestDTO;
import com.ecommerce.E_commerce.dto.auth.AuthResponseDTO;
import com.ecommerce.E_commerce.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO authenticate(AuthRequestDTO authRequest) {
        if (authRequest == null || authRequest.email() == null || authRequest.password() == null) {
            logger.error("Invalid authentication request: null or empty credentials");
            throw new IllegalArgumentException("Email and password cannot be null or empty");
        }

        try {
            logger.debug("Attempting authentication for user: {}", authRequest.email());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.email(),
                            authRequest.password()
                    )
            );

            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                User loggedUser = (User) userDetails;
                String token = jwtService.generateToken(userDetails);
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

                logger.info("Successful authentication for user: {}", authRequest.email());
                return new AuthResponseDTO(token, loggedUser.getFirstName(), loggedUser.getLastName(), userDetails.getUsername(), roles);
            } else {
                logger.error("Authentication principal is not UserDetails for user: {}", authRequest.email());
                throw new IllegalStateException("Authentication principal is not UserDetails");
            }

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - bad credentials", authRequest.email());
            throw new BadCredentialsException("Invalid email or password");
        } catch (Exception e) {
            logger.error("Authentication error for user: {}", authRequest.email(), e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
}