package gr.aueb.budgetmanagement.presentation.api.requests;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsOperationRequest(
    LocalDate date,
    BigDecimal amount
) {}
