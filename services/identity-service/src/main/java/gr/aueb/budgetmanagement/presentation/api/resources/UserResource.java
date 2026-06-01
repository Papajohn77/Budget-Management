package gr.aueb.budgetmanagement.presentation.api.resources;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.representations.AccessTokenRepresentation;
import gr.aueb.budgetmanagement.application.representations.UserRepresentation;
import gr.aueb.budgetmanagement.application.services.UserService;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/users")
public class UserResource {
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/register")
    public Response registerUser(RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
            request.username(),
            request.email(),
            request.password()
        );

        AccessTokenRepresentation result = userService.registerUser(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .build();
    }

    @POST
    @Path("/login")
    public Response authenticateUser(AuthenticateUserRequest request) {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            request.email(),
            request.password()
        );

        AccessTokenRepresentation result = userService.authenticateUser(command);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }

    @GET
    public Response getUserByEmail(@QueryParam("email") String email) {
        UserRepresentation result = userService.getUserByEmail(email);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }
}
