package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;

public record SavingsOperationRepresentation(
    Long id,
    LocalDate date,
    BigDecimal amount,
    SavingsOperationType operationType,
    Long savingsId
) {}
