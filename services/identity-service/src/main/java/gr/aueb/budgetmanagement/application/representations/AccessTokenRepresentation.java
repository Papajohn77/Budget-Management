package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenRepresentation(
    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("access_token")
    String accessToken
) {}
