package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record AddedExpenseRepresentation(
    Long id,
    BigDecimal amount,
    LocalDate date,
    ExpenseCategory category
) {}
