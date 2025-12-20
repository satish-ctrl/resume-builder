package com.satish.resumebuilderapi.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class RegisterRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Name is required")
    @Size(min = 2, max = 15, message = "Name must be between 2 to 15 characters")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 15, message = "password mush be between 6 to 15 characters")
    private String password;

    private String profileImageUrl;
}
