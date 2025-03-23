package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.AddedExpenseRepresentation;
import gr.aueb.budgetmanagement.application.representations.ExpensesRepresentation;
import gr.aueb.budgetmanagement.application.services.ExpenseService;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.presentation.api.requests.AddExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateExpenseRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/api/v1/expenses")
@SecurityRequirement(name = "JWT")
public class ExpenseResource {
    private final ExpenseService expenseService;

    public ExpenseResource(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GET
    @Path("/categories")
    public Response getExpenseCategories() {
        List<String> categories = Arrays.stream(ExpenseCategory.values())
            .map(Enum::name)
            .toList();

        return Response
            .status(Response.Status.OK)
            .entity(Map.of("expense_categories", categories))
            .build();
    }

    @GET
    public Response getExpenses(
        @Context SecurityContext ctx,
        @QueryParam("from_date") LocalDate fromDate,
        @QueryParam("to_date") LocalDate toDate,
        @QueryParam("category") ExpenseCategory category
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        ExpensesRepresentation result = expenseService.getExpenses(
            authenticatedUserId,
            fromDate,
            toDate,
            category
        );

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @POST
    public Response createExpense(@Context SecurityContext ctx, AddExpenseRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AddExpenseCommand command = new AddExpenseCommand(
            new Money(request.amount()),
            request.category(),
            request.date(),
            authenticatedUserId
        );

        AddedExpenseRepresentation result = expenseService.createExpense(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateExpense(
        @Context SecurityContext ctx,
        @PathParam("id") Long expenseId,
        UpdateExpenseRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        UpdateExpenseCommand command = new UpdateExpenseCommand(
            expenseId,
            authenticatedUserId,
            new Money(request.amount()),
            request.date(),
            request.category()
        );

        AddedExpenseRepresentation result = expenseService.updateExpense(command);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteExpense(
        @Context SecurityContext ctx,
        @PathParam("id") Long expenseId
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        expenseService.deleteExpense(expenseId, authenticatedUserId);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }
}
