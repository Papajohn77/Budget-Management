package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.List;

import gr.aueb.budgetmanagement.application.repositories.RecurringExpenseRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class JpaRecurringExpenseRepository implements RecurringExpenseRepository {
    private final EntityManager em;

    public JpaRecurringExpenseRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(RecurringExpense recurringExpense) {
        em.persist(recurringExpense);
    }

    @Override
    public List<RecurringExpense> findNonStoppedRecurringExpenses() {
        return em.createQuery(
            "SELECT re FROM RecurringExpense re WHERE re.isStopped = false", 
            RecurringExpense.class
            )
            .getResultList();
    }
}
