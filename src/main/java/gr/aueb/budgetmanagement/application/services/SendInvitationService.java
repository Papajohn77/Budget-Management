package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.dto.InvitationDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.GroupRepository;
import gr.aueb.budgetmanagement.domain.repositories.InvitationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class SendInvitationService {
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public SendInvitationService(
        InvitationRepository invitationRepository,
        UserRepository userRepository,
        GroupRepository groupRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public InvitationDTO sendInvitation(@Valid SendInvitationCommand command) {
        Group group = groupRepository.findById(command.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found with id: " + command.groupId()));

        User invitee = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new NotFoundException("Invitee not found with email: " + command.email()));

        User admin = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("Admin not found with id: " + command.userId()));

        Invitation invitation = Invitation.create(group, invitee, admin);

        invitationRepository.save(invitation);

        return new InvitationDTO(
            group.getId(),
            group.getAdmin().getEmail(),
            invitee.getEmail(),
            invitation.getStatus(),
            invitation.getCreatedAt()
        );
    }
}