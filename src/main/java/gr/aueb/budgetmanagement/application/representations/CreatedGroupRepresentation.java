package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatedGroupRepresentation(
    Long id,
    String name,
    @JsonProperty("is_admin")
    boolean isAdmin
) {}
