// Service implementation
package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.RespondToInvitationCommand;
import gr.aueb.budgetmanagement.application.dto.InvitationDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class RespondToInvitationService {
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public RespondToInvitationService(
            InvitationRepository invitationRepository,
            UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InvitationDTO respondToInvitation(@Valid RespondToInvitationCommand command) {
        User responder = userRepository.findById(command.responderId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.responderId()));

        InvitationId invitationId = new InvitationId(command.groupId(), command.inviteeId());

        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Invitation not found"));

        responder.respondToInvitation(invitation, command.response());

        invitationRepository.save(invitation);

        return new InvitationDTO(
            invitation.getGroup().getId(),
            invitation.getAdmin().getEmail(),
            invitation.getInvitee().getEmail(),
            invitation.getStatus(),
            invitation.getCreatedAt()
        );
    }
}
