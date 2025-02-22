package gr.aueb.budgetmanagement.application.commands;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateUserCommand(
    @NotBlank(message = "Email is required.")
    String email,

    @NotBlank(message = "Password is required.")
    String password
) {}
