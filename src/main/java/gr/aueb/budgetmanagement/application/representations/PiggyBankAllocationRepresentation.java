package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PiggyBankAllocationRepresentation(
    Long id,
    LocalDate date,
    BigDecimal amount,
    @JsonProperty("piggy_bank_id")
    Long piggyBankId
) {}
