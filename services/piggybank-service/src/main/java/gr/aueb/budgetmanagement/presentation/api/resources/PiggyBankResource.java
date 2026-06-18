package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.DissolvePiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.GroupPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.PersonalPiggyBankRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankAllocationRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBanksRepresentation;
import gr.aueb.budgetmanagement.application.services.PiggyBankAllocationService;
import gr.aueb.budgetmanagement.application.services.PiggyBankService;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.simulation.ConditionSimulator;
import gr.aueb.budgetmanagement.presentation.api.requests.AllocateToPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreatePersonalPiggyBankRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/piggy-banks")
@SecurityRequirement(name = "JWT")
public class PiggyBankResource {
    private final PiggyBankService piggyBankService;
    private final PiggyBankAllocationService piggyBankAllocationService;

    @Inject
    ConditionSimulator conditionSimulator;

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
            new Money(request.targetAmount()),
            request.category(),
            authenticatedUserId
        );

        PersonalPiggyBankRepresentation result = piggyBankService.createPersonalPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePiggyBank(@Context SecurityContext ctx, @PathParam("id") Long piggyBankId) {
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
        CreateGroupPiggyBankRequest request
    ) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            request.name(),
            new Money(request.targetAmount()),
            request.category(),
            groupId,
            authenticatedUserId
        );

        GroupPiggyBankRepresentation result = piggyBankService.createGroupPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @POST
    @Path("/{piggy_bank_id}/allocations")
    public Response allocateToPiggyBank(
        @Context SecurityContext ctx,
        @PathParam("piggy_bank_id") Long piggyBankId,
        AllocateToPiggyBankRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            request.date(),
            new Money(request.amount()),
            piggyBankId,
            authenticatedUserId
        );

        PiggyBankAllocationRepresentation result = piggyBankAllocationService.allocateToPiggyBank(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @GET
    public Response getPiggyBanks(
        @Context SecurityContext ctx, 
        @QueryParam("type") String type
    ) {
        
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(authenticatedUserId, type);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @GET
    @Path("/totals")
    public Response getPiggyBankTotals(
        @Context SecurityContext ctx,
        @QueryParam("user_id") Long userId
    ) {
        conditionSimulator.simulate();

        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }

        PiggyBankTotalsRepresentation result = piggyBankService.getTotals(userId);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }
}