package gr.aueb.budgetmanagement.application.repositories;

import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.User;

public interface UserRepository {
    void save(User user);

    Optional<User> findById(Long id);
}
