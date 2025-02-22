package gr.aueb.budgetmanagement.domain.repositories;

import gr.aueb.budgetmanagement.domain.entities.Group;

public interface GroupRepository {
    void save(Group group);
    boolean existsByNameAndMemberId(String name, Long userId);
}
