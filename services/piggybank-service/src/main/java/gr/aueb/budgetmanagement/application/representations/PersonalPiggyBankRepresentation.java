package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record PersonalPiggyBankRepresentation(
    Long id,

    String name,

    @JsonProperty("target_amount")
    BigDecimal targetAmount,

    @JsonProperty("current_amount")
    BigDecimal currentAmount,

    ExpenseCategory category
) {}
