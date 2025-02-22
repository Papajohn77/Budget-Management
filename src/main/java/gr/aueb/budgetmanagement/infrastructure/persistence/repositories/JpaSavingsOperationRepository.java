package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import gr.aueb.budgetmanagement.domain.entities.SavingsOperation;
import gr.aueb.budgetmanagement.domain.repositories.SavingsOperationRepository;
import jakarta.persistence.EntityManager;

public class JpaSavingsOperationRepository implements SavingsOperationRepository {
    private final EntityManager em;

    public JpaSavingsOperationRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(SavingsOperation operation) {
        em.persist(operation);
    }
}
