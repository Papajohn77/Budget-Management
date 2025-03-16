package gr.aueb.budgetmanagement.presentation.api.resources;

import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.RespondToInvitationCommand;
import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.representations.InvitationRepresentation;
import gr.aueb.budgetmanagement.application.representations.InvitationsRepresentation;
import gr.aueb.budgetmanagement.application.services.InvitationService;
import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
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
    @Path("/{groupId}/{inviteeId}")
    public Response updateInvitationStatus(
            @Context SecurityContext ctx,
            @PathParam("groupId") Long groupId,
            @PathParam("inviteeId") Long inviteeId,
            UpdateInvitationStatusRequest request) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());
        
        if (!authenticatedUserId.equals(inviteeId)) {
            throw new ForbiddenException("Only the invitee can respond to an invitation");
        }
        
        InvitationResponseOperationType operationType;
        try {
            if ("ACCEPTED".equals(request.status())) {
                operationType = InvitationResponseOperationType.ACCEPT;
            } else if ("REJECTED".equals(request.status())) {
                operationType = InvitationResponseOperationType.REJECT;
            } else {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Invalid status: " + request.status()))
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", "Invalid status: " + request.status()))
                .build();
        }
        
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            groupId,
            operationType,
            inviteeId
        );
        
        try {
            InvitationRepresentation result = invitationService.respondToInvitation(command);
            
            return Response
                .status(Response.Status.OK)
                .entity(result)
                .build();
        } catch (NotFoundException e) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", e.getMessage()))
                .build();
        } catch (InvalidDomainArgumentException e) {
            return Response
                .status(Response.Status.CONFLICT)
                .entity(Map.of("message", e.getMessage()))
                .build();
        } catch (ForbiddenOperationDomainException e) {
            return Response
                .status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", e.getMessage()))
                .build();
        }
    }
}