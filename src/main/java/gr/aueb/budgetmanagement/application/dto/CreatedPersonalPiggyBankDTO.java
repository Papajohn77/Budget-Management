package gr.aueb.budgetmanagement.application.dto;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record CreatedPersonalPiggyBankDTO(
    Long id,
    String name,
    Money targetAmount,
    ExpenseCategory category
) {}
