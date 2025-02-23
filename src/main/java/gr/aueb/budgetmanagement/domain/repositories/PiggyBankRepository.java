package gr.aueb.budgetmanagement.domain.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.PiggyBank;

public interface PiggyBankRepository {
    void save(PiggyBank piggyBank);
    Optional<PiggyBank> findById(Long id);
    void delete(PiggyBank piggyBank);
}
