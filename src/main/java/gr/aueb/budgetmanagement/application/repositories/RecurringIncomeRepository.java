package gr.aueb.budgetmanagement.application.repositories;

import java.util.List;

import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;

public interface RecurringIncomeRepository {
    void save(RecurringIncome recurringIncome);
    List<RecurringIncome> findNonStoppedRecurringIncomes();
}
