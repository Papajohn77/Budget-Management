package gr.aueb.budgetmanagement.application.commands;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotNull;

public record AddIncomeCommand(
    @NotNull(message = "Amount cannot be null")
    Money amount,

    @NotNull(message = "Category cannot be null")
    IncomeCategory category,

    @NotNull(message = "Date cannot be null")
    LocalDate date,

    @NotNull(message = "User ID cannot be null")
    Long userId
) {}
