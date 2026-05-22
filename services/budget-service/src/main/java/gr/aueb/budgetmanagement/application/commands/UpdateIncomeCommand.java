package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateIncomeCommand(
    @NotNull
    Long incomeId,

    @NotNull
    Long userId,

    @NotNull
    Money amount,

    @NotNull
    LocalDate date,

    @NotNull
    IncomeCategory category
) {}