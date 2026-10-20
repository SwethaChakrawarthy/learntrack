package com.learntrack.auth.controller;

import com.learntrack.auth.dto.AuthResponse;
import com.learntrack.auth.dto.LoginRequest;
import com.learntrack.auth.dto.RegisterRequest;
import com.learntrack.auth.dto.UserResponse;
import com.learntrack.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getByEmail(authentication.getName());
    }
}
