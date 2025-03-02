package gr.aueb.budgetmanagement.application.representations;

public record CreatedGroupRepresentation(
    Long id,
    String name,
    boolean isAdmin
) {}
