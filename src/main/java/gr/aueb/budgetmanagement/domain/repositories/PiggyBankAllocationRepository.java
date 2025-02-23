package gr.aueb.budgetmanagement.domain.repositories;

import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;

public interface PiggyBankAllocationRepository {
    void save(PiggyBankAllocation allocation);
}
