package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.DissolvePiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.CreatedGroupPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.CreatedPersonalPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankAllocationRepresentation;
import gr.aueb.budgetmanagement.application.services.PiggyBankAllocationService;
import gr.aueb.budgetmanagement.application.services.PiggyBankService;
import gr.aueb.budgetmanagement.presentation.api.requests.AllocateToPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreatePersonalPiggyBankRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/piggy-banks")
@SecurityRequirement(name = "JWT")
public class PiggyBankResource {
    private final PiggyBankService piggyBankService;
    private final PiggyBankAllocationService piggyBankAllocationService;

    public PiggyBankResource(
        PiggyBankService piggyBankService,
        PiggyBankAllocationService piggyBankAllocationService
        ) {
        this.piggyBankService = piggyBankService;
        this.piggyBankAllocationService = piggyBankAllocationService;
    }

    @POST
    public Response createPersonalPiggyBank(@Context SecurityContext ctx, CreatePersonalPiggyBankRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        CreatePersonalPiggyBankCommand command = new CreatePersonalPiggyBankCommand(
            request.name(),
            request.targetAmount(),
            request.category(),
            authenticatedUserId
        );

        CreatedPersonalPiggyBankRepresentation result = piggyBankService.createPersonalPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePersonalPiggyBank(@Context SecurityContext ctx, @PathParam("id") Long piggyBankId) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        DissolvePiggyBankCommand command = new DissolvePiggyBankCommand(
            piggyBankId,
            authenticatedUserId
        );

        piggyBankService.dissolvePiggyBank(command);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }

    @POST
    @Path("/groups/{groupId}/piggy-banks")
    public Response createGroupPiggyBank(
            @Context SecurityContext ctx,
            @PathParam("groupId") Long groupId,
            CreateGroupPiggyBankRequest request) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            request.name(),
            request.targetAmount(),
            request.category(),
            groupId,
            authenticatedUserId
        );

        CreatedGroupPiggyBankRepresentation result = piggyBankService.createGroupPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @DELETE
    @Path("/groups/{groupId}/piggy-banks/{id}")
    public Response deleteGroupPiggyBank(
            @Context SecurityContext ctx,
            @PathParam("groupId") Long groupId,
            @PathParam("id") Long piggyBankId) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        DissolvePiggyBankCommand command = new DissolvePiggyBankCommand(
            piggyBankId,
            authenticatedUserId
        );

        piggyBankService.dissolvePiggyBank(command);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }

    @POST
    @Path("/{piggy_bank_id}/allocations")
    public Response allocateToPiggyBank(
            @Context SecurityContext ctx,
            @PathParam("piggy_bank_id") Long piggyBankId,
            AllocateToPiggyBankRequest request) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            request.date(),
            request.amount(),
            piggyBankId,
            authenticatedUserId
        );

        PiggyBankAllocationRepresentation result = piggyBankAllocationService.allocateToPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }
}