package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record CreatedPersonalPiggyBankRepresentation(
    Long id,
    String name,
    @JsonProperty("target_amount")
    BigDecimal targetAmount,
    ExpenseCategory category
) {}
