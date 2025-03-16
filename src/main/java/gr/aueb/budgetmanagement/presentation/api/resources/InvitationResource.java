package gr.aueb.budgetmanagement.presentation.api.resources;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.InvitationRepresentation;
import gr.aueb.budgetmanagement.application.representations.InvitationsRepresentation;
import gr.aueb.budgetmanagement.application.services.InvitationService;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.presentation.api.requests.SendInvitationRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/invitations")
@SecurityRequirement(name = "JWT")
public class InvitationResource {
    private final InvitationService invitationService;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public InvitationResource(
        InvitationService invitationService, 
        UserRepository userRepository,
        InvitationRepository invitationRepository
    ) {
        this.invitationService = invitationService;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
    }

    @POST
    public Response createInvitation(@Context SecurityContext ctx, SendInvitationRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        SendInvitationCommand command = new SendInvitationCommand(
            request.groupId(), 
            request.email(), 
            authenticatedUserId
        );
        
        InvitationRepresentation result = invitationService.sendInvitation(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }
    
    @GET
    public Response getInvitations(@Context SecurityContext ctx, @QueryParam("status") String status) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());
        
        User user = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + authenticatedUserId));
        
        List<Invitation> invitations;
        if (status != null && status.equals("PENDING")) {
            invitations = invitationRepository.findByInviteeAndStatus(user, InvitationStatus.PENDING);
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
            .collect(Collectors.toList());
        
        InvitationsRepresentation result = new InvitationsRepresentation(invitationRepresentations);
        
        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }
}