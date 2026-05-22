package gr.aueb.budgetmanagement.application.services;

import java.util.List;
import java.util.Set;

import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.GroupRepresentation;
import gr.aueb.budgetmanagement.application.representations.GroupsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.GroupAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GroupRepresentation createGroup(@Valid CreateGroupCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        if (groupRepository.existsByNameAndMemberId(command.name(), user.getId())) {
            throw new GroupAlreadyExistsException("Group with name '" + command.name() + "' already exists for this user");
        }

        Group group = Group.create(command.name(), user);

        groupRepository.save(group);

        return new GroupRepresentation(
            group.getId(),
            group.getName(),
            true
        );
    }

    @Transactional
    public GroupsRepresentation getGroups(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return new GroupsRepresentation(
            toCreatedGroupRepresentationList(user.getGroups(), user)
        );
    }

    private List<GroupRepresentation> toCreatedGroupRepresentationList(Set<Group> groups, User user) {
        return groups.stream()
            .map(group -> new GroupRepresentation(
                group.getId(),
                group.getName(),
                group.isAdmin(user)
            ))
            .toList();
    }
}
