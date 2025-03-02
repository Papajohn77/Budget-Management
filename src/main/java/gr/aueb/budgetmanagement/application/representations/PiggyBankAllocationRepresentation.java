package gr.aueb.budgetmanagement.application.representations;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record PiggyBankAllocationRepresentation(
    Long id,
    LocalDate date,
    Money amount,
    Long piggyBankId
) {}
