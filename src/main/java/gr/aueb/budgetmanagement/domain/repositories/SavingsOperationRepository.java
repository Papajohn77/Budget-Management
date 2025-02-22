package gr.aueb.budgetmanagement.domain.repositories;

import gr.aueb.budgetmanagement.domain.entities.SavingsOperation;

public interface SavingsOperationRepository {
    void save(SavingsOperation operation);
}
