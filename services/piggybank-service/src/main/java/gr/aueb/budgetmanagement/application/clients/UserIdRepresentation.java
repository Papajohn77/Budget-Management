package gr.aueb.budgetmanagement.application.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserIdRepresentation(
    @JsonProperty("user_id")
    Long userId
) {}
