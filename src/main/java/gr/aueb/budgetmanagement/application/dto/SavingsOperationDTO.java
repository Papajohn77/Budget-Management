package gr.aueb.budgetmanagement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;

public record SavingsOperationDTO(
    Long id,
    LocalDate date,
    BigDecimal amount,
    SavingsOperationType operationType,
    Long savingsId
) {}
