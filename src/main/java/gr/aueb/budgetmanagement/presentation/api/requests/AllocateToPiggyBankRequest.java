package gr.aueb.budgetmanagement.presentation.api.requests;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;

public record AllocateToPiggyBankRequest(
    LocalDate date,
    Money amount
) {}