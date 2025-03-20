package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddRecurringExpenseRequest(
    String name,
    LocalDate start_date,
    LocalDate end_date,
    BigDecimal amount,
    ExpenseCategory category
) {
}