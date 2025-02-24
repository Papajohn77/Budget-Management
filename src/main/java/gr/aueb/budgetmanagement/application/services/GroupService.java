package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedGroupDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.validation.Valid;

public class GroupService {
    private final UserRepository userRepository;

    public GroupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CreatedGroupDTO createGroup(@Valid CreateGroupCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        Group group = user.createGroup(command.name());

        userRepository.save(user);

        return new CreatedGroupDTO(
            group.getId(),
            group.getName(),
            true
        );
    }
}
