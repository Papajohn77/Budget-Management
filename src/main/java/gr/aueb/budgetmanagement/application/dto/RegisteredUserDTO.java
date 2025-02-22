package gr.aueb.budgetmanagement.application.dto;

public record RegisteredUserDTO(
    Long id,
    String username,
    String email
) {}
