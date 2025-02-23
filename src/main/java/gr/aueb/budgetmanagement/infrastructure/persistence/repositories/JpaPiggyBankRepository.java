package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import jakarta.persistence.EntityManager;

public class JpaPiggyBankRepository implements PiggyBankRepository {
    private final EntityManager em;

    public JpaPiggyBankRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(PiggyBank piggyBank) {
        em.persist(piggyBank);
    }

    @Override
    public Optional<PiggyBank> findById(Long id) {
        return Optional.ofNullable(em.find(PiggyBank.class, id));
    }

    @Override
    public void delete(PiggyBank piggyBank) {
        em.remove(piggyBank);
    }
}
