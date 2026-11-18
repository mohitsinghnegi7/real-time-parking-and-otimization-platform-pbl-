
package com.pbl.parkingsystem.controller;

import com.pbl.parkingsystem.dto.LoginRequest;
import com.pbl.parkingsystem.dto.LoginResponse;
import com.pbl.parkingsystem.dto.UserRegistrationRequest;
import com.pbl.parkingsystem.dto.UserResponse;
import com.pbl.parkingsystem.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        return ResponseEntity.ok(
                userService.registerUser(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.getCurrentUser(email)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication token is missing"));
        }

        String token = authHeader.substring(7);

        userService.logout(token);

        return ResponseEntity.ok(
                Map.of("message", "Logout successful")
        );
    }
}
