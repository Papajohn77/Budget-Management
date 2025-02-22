package gr.aueb.budgetmanagement.application.dto;

public record CreatedGroupDTO(
    Long id,
    String name,
    boolean isAdmin
) {}
