package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import jakarta.persistence.EntityManager;

public class JpaPiggyBankRepository implements PiggyBankRepository {
    private final EntityManager entityManager;

    public JpaPiggyBankRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(PiggyBank piggyBank) {
        entityManager.persist(piggyBank);
    }

    @Override
    public Optional<PiggyBank> findById(Long id) {
        return Optional.ofNullable(entityManager.find(PiggyBank.class, id));
    }
}
