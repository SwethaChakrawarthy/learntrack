package com.learntrack.auth.service;

import com.learntrack.auth.dto.AuthResponse;
import com.learntrack.auth.dto.LoginRequest;
import com.learntrack.auth.dto.RegisterRequest;
import com.learntrack.auth.exception.EmailAlreadyUsedException;
import com.learntrack.auth.exception.InvalidCredentialsException;
import com.learntrack.auth.model.Role;
import com.learntrack.auth.model.User;
import com.learntrack.auth.repository.UserRepository;
import com.learntrack.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void register_hashesPassword_normalizesEmail_andAssignsStudentRole() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(new RegisterRequest("  Ana@Example.com ", "secret123", "Ana"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("ana@example.com", "secret123", "Ana")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken_whenPasswordMatches() {
        User user = new User("ana@example.com", "hashed", "Ana", Role.STUDENT);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("ana@example.com", "STUDENT")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("ana@example.com", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_rejectsWrongPassword() {
        User user = new User("ana@example.com", "hashed", "Ana", Role.STUDENT);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_rejectsUnknownEmail_withSameError() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "x")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
