package gr.aueb.budgetmanagement.domain.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;

public interface UserRepository {
    void save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(EmailAddress email);
    Optional<User> findByEmail(EmailAddress email);
}
