package gr.aueb.budgetmanagement.presentation.api.requests;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;

public record AddIncomeRequest(
    LocalDate date,
    BigDecimal amount,
    IncomeCategory category
) {}
