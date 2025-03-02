package gr.aueb.budgetmanagement.application.representations;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record AddedExpenseRepresentation(
        Long id,
        Money amount,
        LocalDate date,
        ExpenseCategory category
)
{}