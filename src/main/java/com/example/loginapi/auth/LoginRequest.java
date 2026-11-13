package com.example.loginapi.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Account username", example = "alex")
        @NotBlank String username,
        @Schema(description = "Account password", example = "a-strong-password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank String password
) {
}
