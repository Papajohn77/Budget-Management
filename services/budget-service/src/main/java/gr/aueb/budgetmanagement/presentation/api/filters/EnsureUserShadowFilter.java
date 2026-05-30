package gr.aueb.budgetmanagement.presentation.api.filters;

import org.eclipse.microprofile.jwt.JsonWebToken;

import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class EnsureUserShadowFilter implements ContainerRequestFilter {
    private final JsonWebToken jwt;
    private final UserRepository userRepository;

    public EnsureUserShadowFilter(JsonWebToken jwt, UserRepository userRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void filter(ContainerRequestContext requestContext) {
        if (jwt.getRawToken() == null) {
            return;
        }

        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());
        if (userRepository.findById(authenticatedUserId).isPresent()) {
            return;
        }

        userRepository.save(User.create(authenticatedUserId));
    }
}