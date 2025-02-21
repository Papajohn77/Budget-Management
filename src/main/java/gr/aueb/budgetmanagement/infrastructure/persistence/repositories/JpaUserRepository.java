package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import jakarta.persistence.EntityManager;

public class JpaUserRepository implements UserRepository {
    private final EntityManager em;

    public JpaUserRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(User user) {
        em.persist(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
            .setParameter("username", username)
            .getSingleResult() > 0;
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
            .setParameter("email", email)
            .getSingleResult() > 0;
    }
}
