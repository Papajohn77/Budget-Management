package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record CreatePersonalPiggyBankRequest(
    String name,
    Money targetAmount,
    ExpenseCategory category
) {}