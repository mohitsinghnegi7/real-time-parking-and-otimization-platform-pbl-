
package com.pbl.parkingsystem.service;

import com.pbl.parkingsystem.dto.LoginRequest;
import com.pbl.parkingsystem.dto.LoginResponse;
import com.pbl.parkingsystem.dto.UserRegistrationRequest;
import com.pbl.parkingsystem.dto.UserResponse;
import com.pbl.parkingsystem.entity.RevokedToken;
import com.pbl.parkingsystem.entity.User;
import com.pbl.parkingsystem.repository.RevokedTokenRepository;
import com.pbl.parkingsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    public UserResponse registerUser(
            UserRegistrationRequest registrationRequest) {

        if (userRepository.findByEmail(
                registrationRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email Already Registered");
        }

        User user = new User();
        user.setName(registrationRequest.getName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(
                passwordEncoder.encode(registrationRequest.getPassword())
        );
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());

        return response;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid Email or Password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Email or Password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                "Login Successful",
                user.getId(),
                user.getName(),
                user.getRole(),
                token
        );
    }

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    @Transactional
    public void logout(String token) {

        String tokenHash = jwtService.hashToken(token);

        // Avoid inserting the same token more than once.
        if (revokedTokenRepository.existsById(tokenHash)) {
            return;
        }

        Date expiration = jwtService.extractExpiration(token);

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setTokenHash(tokenHash);
        revokedToken.setExpiresAt(expiration.toInstant());

        revokedTokenRepository.save(revokedToken);
    }
}
