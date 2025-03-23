package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateIncomeRequest(
    LocalDate date,
    BigDecimal amount,
    IncomeCategory category
) {}