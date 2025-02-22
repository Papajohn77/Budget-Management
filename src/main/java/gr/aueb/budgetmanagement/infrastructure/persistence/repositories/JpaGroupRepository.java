package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.repositories.GroupRepository;
import jakarta.persistence.EntityManager;

public class JpaGroupRepository implements GroupRepository {
    private final EntityManager em;

    public JpaGroupRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Group group) {
        em.persist(group);
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

        return em.createQuery(jpql, Boolean.class)
            .setParameter("name", name)
            .setParameter("userId", userId)
            .getSingleResult();
    }

    @Override
    public Optional<Group> findById(Long id) {
        Group group = em.find(Group.class, id);
        return Optional.ofNullable(group);
    }
}
