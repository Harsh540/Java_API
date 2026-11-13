package com.example.loginapi.auth;

import com.example.loginapi.user.AppUser;

public record UserResponse(Long id, String username, String firstName, String lastName, String email) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }
}
