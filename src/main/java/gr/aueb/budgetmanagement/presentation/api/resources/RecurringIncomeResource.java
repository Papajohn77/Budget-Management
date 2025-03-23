package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.RecurringIncomesRepresentation;
import gr.aueb.budgetmanagement.application.services.RecurringIncomeService;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.presentation.api.requests.AddRecurringIncomeRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.StopRecurringIncomeRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/recurring-incomes")
@SecurityRequirement(name = "JWT")
public class RecurringIncomeResource {
    private final RecurringIncomeService recurringIncomeService;

    public RecurringIncomeResource(RecurringIncomeService recurringIncomeService) {
        this.recurringIncomeService = recurringIncomeService;
    }

    @GET
    public Response getRecurringIncomes(@Context SecurityContext ctx) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        RecurringIncomesRepresentation result = recurringIncomeService.getRecurringIncomes(authenticatedUserId);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @POST
    public Response createRecurringIncome(@Context SecurityContext ctx, AddRecurringIncomeRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
            request.name(),
            new Money(request.amount()),
            request.category(),
            request.start_date(),
            request.end_date(),
            authenticatedUserId
        );

        AddedRecurringIncomeRepresentation result = recurringIncomeService.createRecurringIncome(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @PATCH
    @Path("/{id}")
    public Response updateRecurringIncome(
        @Context SecurityContext ctx,
        @PathParam("id") Long recurringIncomeId,
        StopRecurringIncomeRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        UpdateRecurringIncomeCommand command = new UpdateRecurringIncomeCommand(
            recurringIncomeId,
            authenticatedUserId,
            request.is_stopped()
        );

        recurringIncomeService.updateRecurringIncome(command);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRecurringIncome(
        @Context SecurityContext ctx,
        @PathParam("id") Long recurringIncomeId
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        recurringIncomeService.deleteRecurringIncome(recurringIncomeId, authenticatedUserId);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }
}
