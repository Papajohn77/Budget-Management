package gr.aueb.budgetmanagement.application.commands;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserCommand(
    @NotBlank(message = "Username is required")
    String username, 

    @NotBlank(message = "Email is required")
    String email, 

    @NotBlank(message = "Password is required")
    String password
) {}
