package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record CreatedGroupPiggyBankRepresentation(
    Long id,
    String name,
    @JsonProperty("target_amount")
    BigDecimal targetAmount,
    ExpenseCategory category,
    @JsonProperty("group_id")
    Long groupId
) {}
