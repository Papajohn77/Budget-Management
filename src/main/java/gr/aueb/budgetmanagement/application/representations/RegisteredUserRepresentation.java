package gr.aueb.budgetmanagement.application.representations;

public record RegisteredUserRepresentation(
    Long id,
    String username,
    String email
) {}
