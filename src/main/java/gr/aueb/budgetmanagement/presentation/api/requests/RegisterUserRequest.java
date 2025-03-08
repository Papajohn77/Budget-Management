package gr.aueb.budgetmanagement.presentation.api.requests;

public record RegisterUserRequest(
    String username,
    String email,
    String password
) {}
