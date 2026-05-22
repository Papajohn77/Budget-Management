package gr.aueb.budgetmanagement.application.commands;

public record UpdateRecurringIncomeCommand(
    Long recurringIncomeId,
    Long userId,
    boolean isStopped
) {}
