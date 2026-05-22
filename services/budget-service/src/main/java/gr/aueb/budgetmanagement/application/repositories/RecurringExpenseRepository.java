package gr.aueb.budgetmanagement.application.repositories;

import java.util.List;

import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;

public interface RecurringExpenseRepository {
    void save(RecurringExpense recurringExpense);
    List<RecurringExpense> findNonStoppedRecurringExpenses();
}
