package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.AddedIncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.IncomesRepresentation;
import gr.aueb.budgetmanagement.application.services.IncomeService;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.presentation.api.requests.AddIncomeRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateIncomeRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/api/v1/incomes")
@SecurityRequirement(name = "JWT")
public class IncomeResource {
    private final IncomeService incomeService;

    public IncomeResource(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GET
    @Path("/categories")
    public Response getIncomeCategories() {
        List<String> categories = Arrays.stream(IncomeCategory.values())
            .map(Enum::name)
            .toList();

        return Response
            .status(Response.Status.OK)
            .entity(Map.of("income_categories", categories))
            .build();
    }

    @GET
    public Response getIncomes(
        @Context SecurityContext ctx,
        @QueryParam("from_date") LocalDate fromDate,
        @QueryParam("to_date") LocalDate toDate,
        @QueryParam("category") IncomeCategory category
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        IncomesRepresentation result = incomeService.getIncomes(
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
    public Response createIncome(@Context SecurityContext ctx, AddIncomeRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        AddIncomeCommand command = new AddIncomeCommand(
            new Money(request.amount()),
            request.category(),
            request.date(),
            authenticatedUserId
        );

        AddedIncomeRepresentation result = incomeService.createIncome(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateIncome(
        @Context SecurityContext ctx,
        @PathParam("id") Long incomeId,
        UpdateIncomeRequest request
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        UpdateIncomeCommand command = new UpdateIncomeCommand(
            incomeId,
            authenticatedUserId,
            new Money(request.amount()),
            request.date(),
            request.category()
        );

        AddedIncomeRepresentation result = incomeService.updateIncome(command);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteIncome(
        @Context SecurityContext ctx,
        @PathParam("id") Long incomeId
    ) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        incomeService.deleteIncome(incomeId, authenticatedUserId);

        return Response
            .status(Response.Status.NO_CONTENT)
            .build();
    }
}
