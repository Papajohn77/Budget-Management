package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.List;

import gr.aueb.budgetmanagement.application.repositories.RecurringIncomeRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class JpaRecurringIncomeRepository implements RecurringIncomeRepository {
    private final EntityManager em;

    public JpaRecurringIncomeRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(RecurringIncome recurringIncome) {
        em.persist(recurringIncome);
    }

    @Override
    public List<RecurringIncome> findNonStoppedRecurringIncomes() {
        return em.createQuery(
            "SELECT ri FROM RecurringIncome ri WHERE ri.isStopped = false", 
                RecurringIncome.class
            )
            .getResultList();
    }
}
