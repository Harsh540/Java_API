package com.example.loginapi.auth;

import com.example.loginapi.user.AppUser;
import com.example.loginapi.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_NAME_LENGTH = 80;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(RegisterRequest request) {
        String username = safeTrim(request.username());
        String firstName = safeTrim(request.firstName());
        String lastName = safeTrim(request.lastName());
        String email = safeTrim(request.email()).toLowerCase(Locale.ROOT);
        String password = request.password();

        validateUsername(username);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateEmail(email);
        validatePassword(password);

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email is already in use");
        }

        AppUser user = new AppUser(
                username,
                firstName,
                lastName,
                email,
                passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public AppUser authenticate(LoginRequest request) {
        String username = safeTrim(request.username());
        String password = request.password();

        if (password == null || password.isBlank() || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return user;
    }

    public AppUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in"));
    }

    private void validateUsername(String username) {
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be 3 to 50 characters");
        }
    }

    private void validateName(String value, String fieldName) {
        if (value.isBlank() || value.length() > MAX_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be 1 to 80 characters");
        }
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be a valid email address");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be 8 to 72 characters");
        }
        int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (passwordBytes < MIN_PASSWORD_LENGTH || passwordBytes > MAX_PASSWORD_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be 8 to 72 characters");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
