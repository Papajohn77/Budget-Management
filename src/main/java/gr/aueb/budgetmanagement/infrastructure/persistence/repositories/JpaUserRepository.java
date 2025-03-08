package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
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
    public boolean existsByEmail(String email) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
            .setParameter("email", new EmailAddress(email))
            .getSingleResult() > 0;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> results = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
            .setParameter("email", new EmailAddress(email))
            .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }
}
