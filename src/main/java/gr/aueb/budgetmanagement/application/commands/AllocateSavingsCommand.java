package gr.aueb.budgetmanagement.application.commands;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotNull;

public record AllocateSavingsCommand(
    @NotNull(message = "Amount is required.")
    Money amount,

    @NotNull(message = "Date is required.")
    LocalDate date,

    @NotNull(message = "User ID is required.")
    Long userId
) {}
