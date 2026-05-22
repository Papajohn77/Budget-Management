package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AllocateSavingsCommand;
import gr.aueb.budgetmanagement.application.commands.DeallocateSavingsCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.SavingsOperationRepresentation;
import gr.aueb.budgetmanagement.application.representations.SavingsRepresentation;
import gr.aueb.budgetmanagement.application.services.SavingsOperationService;
import gr.aueb.budgetmanagement.application.services.SavingsService;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.presentation.api.requests.SavingsOperationRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/savings")
@SecurityRequirement(name = "JWT")
public class SavingsResource {
    private final SavingsService savingsService;
    private final SavingsOperationService savingsOperationService;

    public SavingsResource(
        SavingsService savingsService,
        SavingsOperationService savingsOperationService
    ) {
        this.savingsService = savingsService;
        this.savingsOperationService = savingsOperationService;
    }

    @GET
    public Response getSavings(@Context SecurityContext ctx) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        SavingsRepresentation result = savingsService.getSavings(authenticatedUserId);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @POST
    @Path("/allocations")
    public Response allocateToSavings(@Context SecurityContext ctx, SavingsOperationRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AllocateSavingsCommand command = new AllocateSavingsCommand(
            new Money(request.amount()), 
            request.date(), 
            authenticatedUserId
        );
        SavingsOperationRepresentation result = savingsOperationService.allocate(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @POST
    @Path("/deallocations")
    public Response deallocateFromSavings(@Context SecurityContext ctx, SavingsOperationRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        DeallocateSavingsCommand command = new DeallocateSavingsCommand(
            new Money(request.amount()), 
            request.date(), 
            authenticatedUserId
        );
        SavingsOperationRepresentation result = savingsOperationService.deallocate(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }
}
