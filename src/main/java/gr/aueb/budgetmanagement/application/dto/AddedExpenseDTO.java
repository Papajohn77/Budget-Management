package gr.aueb.budgetmanagement.application.dto;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record AddedExpenseDTO(
        Long id,
        Money amount,
        LocalDate date,
        ExpenseCategory category
)
{}