package gr.aueb.budgetmanagement.presentation.api.requests;

public record AuthenticateUserRequest(
    String email,
    String password
) {}
