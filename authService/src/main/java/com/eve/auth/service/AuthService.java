package com.eve.auth.service;

import com.eve.auth.dto.LoginRequest;
import com.eve.auth.dto.LoginResponse;
import com.eve.auth.dto.SignupRequest;
import com.eve.auth.dto.SignupResponse;
import com.eve.auth.entity.User;
import com.eve.auth.exception.UserAlreadyExistsException;
import com.eve.auth.repository.UserRepository;
import com.eve.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public SignupResponse signup(SignupRequest request) {

        String normalizedEmail =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        String normalizedName =
                request.getName().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + normalizedEmail
            );
        }

        User user = User.builder()
                .name(normalizedName)
                .email(normalizedEmail)
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return SignupResponse.builder()
                .message("User registered successfully")
                .token(token)
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    public LoginResponse login(LoginRequest request) {

        if (request == null ||
                request.getEmail() == null ||
                request.getPassword() == null) {

            throw new IllegalArgumentException(
                    "Email and password are required"
            );
        }

        String email =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Make sure user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        // Authenticate email + password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.getPassword()
                        )
                );

        // Authentication successful
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}