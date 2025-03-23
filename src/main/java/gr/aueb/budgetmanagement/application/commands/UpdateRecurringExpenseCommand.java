package gr.aueb.budgetmanagement.application.commands;

public record UpdateRecurringExpenseCommand(
    Long recurringExpenseId,
    Long userId,
    boolean isStopped
) {}