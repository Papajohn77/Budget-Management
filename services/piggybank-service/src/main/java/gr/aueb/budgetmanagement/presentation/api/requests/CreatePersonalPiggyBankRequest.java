package gr.aueb.budgetmanagement.presentation.api.requests;

import java.math.BigDecimal;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record CreatePersonalPiggyBankRequest(
    String name,
    BigDecimal targetAmount,
    ExpenseCategory category
) {}