package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AddRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.RecurringExpenseRepresentation;
import gr.aueb.budgetmanagement.application.representations.RecurringExpensesRepresentation;
import gr.aueb.budgetmanagement.application.services.RecurringExpenseService;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.presentation.api.requests.AddRecurringExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.StopRecurringExpenseRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/recurring-expenses")
@SecurityRequirement(name = "JWT")
public class RecurringExpenseResource {
    private final RecurringExpenseService recurringExpenseService;

    public RecurringExpenseResource(RecurringExpenseService recurringExpenseService) {
        this.recurringExpenseService = recurringExpenseService;
    }

    @GET
    public Response getRecurringExpenses(@Context SecurityContext ctx) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        RecurringExpensesRepresentation result = recurringExpenseService.getRecurringExpenses(authenticatedUserId);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @POST
    public Response createRecurringExpense(@Context SecurityContext ctx, AddRecurringExpenseRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
            request.name(),
            new Money(request.amount()),
            request.category(),
            request.start_date(),
            request.end_date(),
            authenticatedUserId
        );

        RecurringExpenseRepresentation result = recurringExpenseService.createRecurringExpense(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @PATCH
    @Path("/{id}")
    public Response updateRecurringExpense(
        @Context SecurityContext ctx,
        @PathParam("id") Long recurringExpenseId,
        StopRecurringExpenseRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        UpdateRecurringExpenseCommand command = new UpdateRecurringExpenseCommand(
            recurringExpenseId,
            authenticatedUserId,
            request.is_stopped()
        );

        recurringExpenseService.updateRecurringExpense(command);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRecurringExpense(
        @Context SecurityContext ctx,
        @PathParam("id") Long recurringExpenseId
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        recurringExpenseService.deleteRecurringExpense(recurringExpenseId, authenticatedUserId);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }
}
