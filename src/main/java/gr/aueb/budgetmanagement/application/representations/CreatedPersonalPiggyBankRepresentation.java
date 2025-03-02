package gr.aueb.budgetmanagement.application.representations;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record CreatedPersonalPiggyBankRepresentation(
    Long id,
    String name,
    Money targetAmount,
    ExpenseCategory category
) {}
