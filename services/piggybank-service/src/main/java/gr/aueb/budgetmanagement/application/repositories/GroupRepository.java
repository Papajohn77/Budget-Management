package gr.aueb.budgetmanagement.application.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.Group;

public interface GroupRepository {
    void save(Group group);
    boolean existsByNameAndMemberId(String name, Long userId);
    Optional<Group> findById(Long id);
}
