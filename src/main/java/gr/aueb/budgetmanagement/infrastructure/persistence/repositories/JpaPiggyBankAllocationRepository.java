package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankAllocationRepository;
import jakarta.persistence.EntityManager;

public class JpaPiggyBankAllocationRepository implements PiggyBankAllocationRepository {
    private final EntityManager em;

    public JpaPiggyBankAllocationRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(PiggyBankAllocation allocation) {
        em.persist(allocation);
    }
}
