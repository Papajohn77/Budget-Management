package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
    LocalDate date,
    BigDecimal amount,
    ExpenseCategory category
) {}
