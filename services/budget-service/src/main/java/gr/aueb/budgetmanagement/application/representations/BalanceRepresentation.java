package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BalanceRepresentation(
    @JsonProperty("balance")
    BigDecimal balance
) {}
