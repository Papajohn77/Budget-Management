package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.PiggyBankAllocationRepresentation;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class PiggyBankAllocationService {
    private final PiggyBankRepository piggyBankRepository;
    private final UserRepository userRepository;

    public PiggyBankAllocationService(
        PiggyBankRepository piggyBankRepository,
        UserRepository userRepository
    ) {
        this.piggyBankRepository = piggyBankRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PiggyBankAllocationRepresentation allocateToPiggyBank(
        @Valid AllocateToPiggyBankCommand command
    ) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        PiggyBank piggyBank = piggyBankRepository.findById(command.piggyBankId())
            .orElseThrow(() -> new NotFoundException("Piggy bank not found with id: " + command.piggyBankId()));

        PiggyBankAllocation allocation = piggyBank.allocate(
            command.amount(),
            command.date(),
            user
        );

        piggyBankRepository.save(piggyBank);

        return new PiggyBankAllocationRepresentation(
            allocation.getId(),
            allocation.getDate(),
            allocation.getAmount(),
            allocation.getPiggyBank().getId()
        );
    }
}
