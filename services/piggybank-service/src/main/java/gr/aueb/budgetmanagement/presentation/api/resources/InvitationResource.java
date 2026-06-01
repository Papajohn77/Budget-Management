package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.RespondToInvitationCommand;
import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.InvitationRepresentation;
import gr.aueb.budgetmanagement.application.representations.InvitationsRepresentation;
import gr.aueb.budgetmanagement.application.services.InvitationService;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.presentation.api.requests.SendInvitationRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateInvitationStatusRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/invitations")
@SecurityRequirement(name = "JWT")
public class InvitationResource {
    private final InvitationService invitationService;

    public InvitationResource(InvitationService invitationService) {
        this.invitationService = invitationService;
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
    public Response getInvitations(@Context SecurityContext ctx, @QueryParam("status") InvitationStatus status) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());
        
        InvitationsRepresentation result = invitationService.getInvitations(authenticatedUserId, status);
        
        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @PATCH
    @Path("/{group_id}")
    public Response updateInvitationStatus(
        @Context SecurityContext ctx,
        @PathParam("group_id") Long groupId,
        UpdateInvitationStatusRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());
        
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            groupId,
            request.status(),
            authenticatedUserId
        );
        
        InvitationRepresentation result = invitationService.respondToInvitation(command);
        
        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }
}
