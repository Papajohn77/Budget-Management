package gr.aueb.budgetmanagement.application.services;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import gr.aueb.budgetmanagement.application.clients.IdentityClient;
import gr.aueb.budgetmanagement.application.clients.UserIdRepresentation;
import gr.aueb.budgetmanagement.application.commands.RespondToInvitationCommand;
import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.InvitationRepresentation;
import gr.aueb.budgetmanagement.application.representations.InvitationsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final IdentityClient identityClient;

    public InvitationService(
        InvitationRepository invitationRepository,
        GroupRepository groupRepository,
        UserRepository userRepository,
        @RestClient IdentityClient identityClient
    ) {
        this.invitationRepository = invitationRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.identityClient = identityClient;
    }

    @Transactional
    public InvitationRepresentation sendInvitation(@Valid SendInvitationCommand command) {
        Group group = groupRepository.findById(command.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found with id: " + command.groupId()));

        UserIdRepresentation inviteeAccount;
        try {
            inviteeAccount = identityClient.findByEmail(command.email());
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                throw new NotFoundException("Invitee not found with email: " + command.email());
            }
            throw e;
        }

        User invitee = userRepository.findById(inviteeAccount.userId())
            .orElseGet(() -> {
                User newUser = User.create(inviteeAccount.userId());
                userRepository.save(newUser);
                return newUser;
            });

        User admin = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("Admin not found with id: " + command.userId()));

        Invitation invitation = Invitation.create(group, invitee, admin);

        invitationRepository.save(invitation);

        return new InvitationRepresentation(
            group.getId(),
            invitee.getId(),
            invitation.getStatus(),
            invitation.getCreatedAt()
        );
    }

    @Transactional
    public InvitationRepresentation respondToInvitation(@Valid RespondToInvitationCommand command) {
        Group group = groupRepository.findById(command.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found with id: " + command.groupId()));

        User invitee = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        InvitationId invitationId = new InvitationId(group.getId(), invitee.getId());
        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Invitation not found"));

        invitation.respond(command.response(), invitee);

        invitationRepository.save(invitation);

        return new InvitationRepresentation(
            invitation.getGroup().getId(),
            invitation.getInvitee().getId(),
            invitation.getStatus(),
            invitation.getCreatedAt()
        );
    }

    @Transactional
    public InvitationsRepresentation getInvitations(Long userId, InvitationStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Invitation> invitations;
        if (status != null) {
            invitations = invitationRepository.findByInviteeAndStatus(user, status);
        } else {
            invitations = invitationRepository.findByInvitee(user);
        }

        List<InvitationRepresentation> invitationRepresentations = invitations.stream()
            .map(invitation -> new InvitationRepresentation(
                invitation.getGroup().getId(),
                invitation.getInvitee().getId(),
                invitation.getStatus(),
                invitation.getCreatedAt()
            ))
            .toList();

        return new InvitationsRepresentation(invitationRepresentations);
    }
}
