package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;

public record SavingsOperationRepresentation(
    Long id,

    LocalDate date,

    BigDecimal amount,

    @JsonProperty("operation_type")
    SavingsOperationType operationType,

    @JsonProperty("savings_id")
    Long savingsId
) {}
