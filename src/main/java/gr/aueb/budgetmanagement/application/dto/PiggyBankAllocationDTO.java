package gr.aueb.budgetmanagement.application.dto;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record PiggyBankAllocationDTO(
    Long id,
    LocalDate date,
    Money amount,
    Long piggyBankId
) {}
