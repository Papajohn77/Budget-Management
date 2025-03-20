package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddIncomeRequest(
    LocalDate date,
    BigDecimal amount,
    IncomeCategory category
) {}
