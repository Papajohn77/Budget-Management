package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.repositories.GroupRepository;
import jakarta.persistence.EntityManager;

public class JpaGroupRepository implements GroupRepository {
    private final EntityManager entityManager;

    public JpaGroupRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Group group) {
        entityManager.persist(group);
    }

    @Override
    public boolean existsByNameAndMemberId(String name, Long userId) {
        String jpql = """
            SELECT COUNT(g) > 0 
            FROM Group g 
            JOIN g.members m 
            WHERE g.name = :name 
            AND m.id = :userId
        """;

        return entityManager.createQuery(jpql, Boolean.class)
            .setParameter("name", name)
            .setParameter("userId", userId)
            .getSingleResult();
    }
}