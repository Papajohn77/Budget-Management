package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PiggyBankTotalsRepresentation(
    @JsonProperty("total")
    BigDecimal total
) {}
