package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedGroupPiggyBankDTO;
import gr.aueb.budgetmanagement.application.dto.CreatedPersonalPiggyBankDTO;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.GroupRepository;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import jakarta.transaction.Transactional;

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
    public CreatedPersonalPiggyBankDTO createPersonalPiggyBank(CreatePersonalPiggyBankCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            command.name(),
            command.targetAmount(),
            command.category(),
            user
        );

        piggyBankRepository.save(piggyBank);

        return new CreatedPersonalPiggyBankDTO(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount(),
            piggyBank.getCategory()
        );
    }

    @Transactional
    public CreatedGroupPiggyBankDTO createGroupPiggyBank(CreateGroupPiggyBankCommand command) {
        Group group = groupRepository.findById(command.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found"));

        User admin = userRepository.findById(command.adminId())
            .orElseThrow(() -> new NotFoundException("Admin user not found"));

        if (!group.getAdmin().equals(admin)) {
            throw new ForbiddenException("Only group admin can create group piggy banks");
        }

        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            command.name(),
            command.targetAmount(),
            command.category(),
            group
        );

        piggyBankRepository.save(piggyBank);

        return new CreatedGroupPiggyBankDTO(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount(),
            piggyBank.getCategory(),
            group.getId()
        );
    }
}
