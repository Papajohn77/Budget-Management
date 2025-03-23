package gr.aueb.budgetmanagement.application.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;

public interface UserRepository {
    void save(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<RecurringExpense> findRecurringExpenseById(Long aLong);
    Optional<RecurringIncome> findRecurringIncomeById(Long aLong);

}
