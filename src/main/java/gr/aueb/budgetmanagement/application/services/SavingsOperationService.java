package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AllocateSavingsCommand;
import gr.aueb.budgetmanagement.application.commands.DeallocateSavingsCommand;
import gr.aueb.budgetmanagement.application.dto.SavingsOperationDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.SavingsOperation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class SavingsOperationService {
    private final UserRepository userRepository;

    public SavingsOperationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public SavingsOperationDTO allocate(@Valid AllocateSavingsCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        SavingsOperation operation = user.allocateSavings(
            new Money(command.amount()),
            command.date()
        );

        userRepository.save(user);

        return new SavingsOperationDTO(
            operation.getId(),
            operation.getDate(),
            operation.getAmount().getValue(),
            operation.getOperation(),
            operation.getSavings().getId()
        );
    }

    @Transactional
    public SavingsOperationDTO deallocate(@Valid DeallocateSavingsCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        SavingsOperation operation = user.deallocateSavings(
            new Money(command.amount()),
            command.date()
        );

        userRepository.save(user);

        return new SavingsOperationDTO(
            operation.getId(),
            operation.getDate(),
            operation.getAmount().getValue(),
            operation.getOperation(),
            operation.getSavings().getId()
        );
    }
}
