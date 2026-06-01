package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRepresentation(
    @JsonProperty("user_id")
    Long userId
) {}
