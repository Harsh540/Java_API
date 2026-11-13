package com.example.loginapi.auth;

import com.example.loginapi.user.AppUser;
import com.example.loginapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    @Test
    void authenticatesMatchingPassword() {
        AppUser user = new AppUser("alex", "Alex", "Morgan", "alex@example.com",
                passwordEncoder.encode("correct-password"));
        when(userRepository.findByUsername("alex")).thenReturn(Optional.of(user));

        assertEquals(user, authService.authenticate(new LoginRequest("alex", "correct-password")));
    }

    @Test
    void rejectsIncorrectPassword() {
        AppUser user = new AppUser("alex", "Alex", "Morgan", "alex@example.com",
                passwordEncoder.encode("correct-password"));
        when(userRepository.findByUsername("alex")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.authenticate(new LoginRequest("alex", "wrong-password")));

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void storesOnlyEncodedPasswordWhenRegistering() {
        when(userRepository.existsByUsername("alex")).thenReturn(false);
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = authService.register(new RegisterRequest(
                " alex ", " Alex ", " Morgan ", "Alex@Example.com", "correct-password"));

        assertEquals("alex", user.getUsername());
        assertEquals("Alex", user.getFirstName());
        assertEquals("Morgan", user.getLastName());
        assertEquals("alex@example.com", user.getEmail());
        assertTrue(passwordEncoder.matches("correct-password", user.getPasswordHash()));
    }

    @Test
    void rejectsLoginWhenUserIsNotRegistered() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.authenticate(new LoginRequest("unknown", "correct-password")));

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void rejectsEmailAlreadyUsedByAnotherAccount() {
        when(userRepository.existsByUsername("alex")).thenReturn(false);
        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new RegisterRequest(
                        "alex", "Alex", "Morgan", "alex@example.com", "correct-password")));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void rejectsPasswordsShorterThanEightCharacters() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new RegisterRequest(
                        "alex", "Alex", "Morgan", "alex@example.com", "short")));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidEmailAddresses() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new RegisterRequest(
                        "alex", "Alex", "Morgan", "not-an-email", "correct-password")));

        assertEquals(400, exception.getStatusCode().value());
    }
}
