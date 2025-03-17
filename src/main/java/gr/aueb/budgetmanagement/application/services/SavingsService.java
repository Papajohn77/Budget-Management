package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.SavingsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Savings;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SavingsService {
    private final UserRepository userRepository;

    public SavingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public SavingsRepresentation getSavings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Savings savings = user.getSavings();

        return new SavingsRepresentation(savings.getId(), savings.getCurrentAmount().getValue());
    }
}
