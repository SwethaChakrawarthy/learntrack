package com.learntrack.auth.service;

import com.learntrack.auth.dto.AuthResponse;
import com.learntrack.auth.dto.LoginRequest;
import com.learntrack.auth.dto.RegisterRequest;
import com.learntrack.auth.dto.UserResponse;
import com.learntrack.auth.exception.EmailAlreadyUsedException;
import com.learntrack.auth.exception.InvalidCredentialsException;
import com.learntrack.auth.model.Role;
import com.learntrack.auth.model.User;
import com.learntrack.auth.repository.UserRepository;
import com.learntrack.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        // New accounts are always STUDENT; instructor accounts are granted separately.
        User user = new User(email, passwordEncoder.encode(request.password()),
                request.fullName().trim(), Role.STUDENT);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
