package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.CreatedGroupRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.GroupAlreadyExistsException;
import jakarta.validation.Valid;

public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public CreatedGroupRepresentation createGroup(@Valid CreateGroupCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        if (groupRepository.existsByNameAndMemberId(command.name(), user.getId())) {
            throw new GroupAlreadyExistsException("Group with name '" + command.name() + "' already exists for this user");
        }

        Group group = Group.create(command.name(), user);

        groupRepository.save(group);

        return new CreatedGroupRepresentation(
            group.getId(),
            group.getName(),
            true
        );
    }
}
