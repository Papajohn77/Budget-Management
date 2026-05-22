package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateExpenseCommand(
    @NotNull
    Long expenseId,

    @NotNull
    Long userId,

    @NotNull
    Money amount,

    @NotNull
    LocalDate date,

    @NotNull
    ExpenseCategory category
) {}