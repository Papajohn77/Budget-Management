package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SavingsRepresentation(
    Long id,

    @JsonProperty("current_amount")
    BigDecimal currentAmount
) {}
