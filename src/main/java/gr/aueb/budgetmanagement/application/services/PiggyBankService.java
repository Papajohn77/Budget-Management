package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.DissolvePiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.CreatedGroupPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.CreatedPersonalPiggyBankRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class PiggyBankService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PiggyBankRepository piggyBankRepository;

    public PiggyBankService(
        UserRepository userRepository, 
        GroupRepository groupRepository,
        PiggyBankRepository piggyBankRepository
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.piggyBankRepository = piggyBankRepository;
    }

    @Transactional
    public CreatedPersonalPiggyBankRepresentation createPersonalPiggyBank(
        @Valid CreatePersonalPiggyBankCommand command
    ) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            command.name(),
            command.targetAmount(),
            command.category(),
            user
        );

        piggyBankRepository.save(piggyBank);

        return new CreatedPersonalPiggyBankRepresentation(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount(),
            piggyBank.getCategory()
        );
    }

    @Transactional
    public CreatedGroupPiggyBankRepresentation createGroupPiggyBank(
        @Valid CreateGroupPiggyBankCommand command
    ) {
        Group group = groupRepository.findById(command.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found with id: " + command.groupId()));

        User admin = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("Admin user not found with id: " + command.userId()));

        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            command.name(),
            command.targetAmount(),
            command.category(),
            group, 
            admin
        );

        piggyBankRepository.save(piggyBank);

        return new CreatedGroupPiggyBankRepresentation(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount(),
            piggyBank.getCategory(),
            group.getId()
        );
    }

    @Transactional
    public void dissolvePiggyBank(@Valid DissolvePiggyBankCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        PiggyBank piggyBank = piggyBankRepository.findById(command.piggyBankId())
            .orElseThrow(() -> new NotFoundException("Piggy bank not found with id: " + command.piggyBankId()));

        if (!piggyBank.canBeDissolvedBy(user)) {
            throw new ForbiddenException("User is not authorized to dissolve this piggy bank");
        }

        piggyBankRepository.delete(piggyBank);
    }
}
