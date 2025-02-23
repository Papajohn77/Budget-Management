package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.dto.PiggyBankAllocationDTO;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankAllocationRepository;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class PiggyBankAllocationService {
    private final PiggyBankAllocationRepository allocationRepository;
    private final PiggyBankRepository piggyBankRepository;
    private final UserRepository userRepository;

    public PiggyBankAllocationService(
        PiggyBankAllocationRepository allocationRepository,
        PiggyBankRepository piggyBankRepository,
        UserRepository userRepository
    ) {
        this.allocationRepository = allocationRepository;
        this.piggyBankRepository = piggyBankRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PiggyBankAllocationDTO allocateToPiggyBank(@Valid AllocateToPiggyBankCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        PiggyBank piggyBank = piggyBankRepository.findById(command.piggyBankId())
            .orElseThrow(() -> new NotFoundException("Piggy bank not found with id: " + command.piggyBankId()));

        if (!piggyBank.isAuthorizedUser(user)) {
            throw new ForbiddenException("User is not authorized to access this piggy bank");
        }

        PiggyBankAllocation allocation = PiggyBankAllocation.create(
            command.amount(),
            command.date(),
            piggyBank,
            user
        );

        allocationRepository.save(allocation);

        return new PiggyBankAllocationDTO(
            allocation.getId(),
            allocation.getDate(),
            allocation.getAmount(),
            allocation.getPiggyBank().getId()
        );
    }
}
