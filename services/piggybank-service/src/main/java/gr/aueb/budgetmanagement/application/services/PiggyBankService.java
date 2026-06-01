package gr.aueb.budgetmanagement.application.services;

import java.math.BigDecimal;
import java.util.List;

import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.DissolvePiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.GroupPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.PersonalPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.GroupPiggyBanksRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBanksRepresentation;
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
    public PersonalPiggyBankRepresentation createPersonalPiggyBank(
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

        return new PersonalPiggyBankRepresentation(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount().getValue(),
            piggyBank.getCurrentAmount().getValue(),
            piggyBank.getCategory()
        );
    }

    @Transactional
    public GroupPiggyBankRepresentation createGroupPiggyBank(
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

        return new GroupPiggyBankRepresentation(
            piggyBank.getId(),
            piggyBank.getName(),
            piggyBank.getTargetAmount().getValue(),
            piggyBank.getCurrentAmount().getValue(),
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

    @Transactional
    public PiggyBanksRepresentation getPiggyBanks(Long userId, String type) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<PersonalPiggyBankRepresentation> personalPiggyBanks = List.of();
        List<GroupPiggyBanksRepresentation> groupPiggyBanks = List.of();
        
        // Get personal piggy banks if needed
        if (type == null || "personal".equalsIgnoreCase(type)) {
            personalPiggyBanks = user.getPiggyBanks().stream()
                .map(piggyBank -> new PersonalPiggyBankRepresentation(
                    piggyBank.getId(),
                    piggyBank.getName(),
                    piggyBank.getTargetAmount().getValue(),
                    piggyBank.getCurrentAmount().getValue(),
                    piggyBank.getCategory()
                ))
                .toList();
        }
        
        // Get group piggy banks if needed
        if (type == null || "group".equalsIgnoreCase(type)) {
            groupPiggyBanks = user.getGroups().stream()
                .map(group -> new GroupPiggyBanksRepresentation(
                    group.getName(),
                    group.getId(),
                    group.getPiggyBanks().stream()
                        .map(piggyBank -> new GroupPiggyBankRepresentation(
                            piggyBank.getId(), 
                            piggyBank.getName(),
                            piggyBank.getTargetAmount().getValue(), 
                            piggyBank.getCurrentAmount().getValue(),
                            piggyBank.getCategory(),
                            group.getId()
                        ))
                        .toList()
                ))
                .toList();
        }

        return new PiggyBanksRepresentation(personalPiggyBanks, groupPiggyBanks);
    }

    @Transactional
    public PiggyBankTotalsRepresentation getTotals(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        BigDecimal total = user.getPiggyBanks().stream()
            .map(pb -> pb.getCurrentAmount().getValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PiggyBankTotalsRepresentation(total);
    }
}
