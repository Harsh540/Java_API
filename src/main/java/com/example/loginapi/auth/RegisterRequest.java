package com.example.loginapi.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(description = "Username, 3 to 50 characters", example = "alex")
        @NotBlank @Size(min = 3, max = 50) String username,
        @Schema(description = "First name", example = "Alex")
        @NotBlank @Size(max = 80) String firstName,
        @Schema(description = "Last name", example = "Morgan")
        @NotBlank @Size(max = 80) String lastName,
        @Schema(description = "Unique email address", example = "alex@example.com")
        @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "Password, 8 to 72 characters and at most 72 UTF-8 bytes",
                example = "a-strong-password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
