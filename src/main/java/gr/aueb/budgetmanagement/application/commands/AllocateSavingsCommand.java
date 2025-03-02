package gr.aueb.budgetmanagement.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record AllocateSavingsCommand(
    @NotNull(message = "Amount is required.")
    BigDecimal amount,

    @NotNull(message = "Date is required.")
    LocalDate date,

    @NotNull(message = "User ID is required.")
    Long userId
) {}
