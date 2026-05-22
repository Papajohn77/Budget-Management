package gr.aueb.budgetmanagement.infrastructure.security;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import gr.aueb.budgetmanagement.domain.entities.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtTokenService {
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "mp.jwt.verify.token.age")
    int tokenAgeSeconds;

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofSeconds(tokenAgeSeconds));

        return Jwt.issuer(issuer)
            .subject(user.getEmail().getValue())
            .claim("user_id", user.getId())
            .issuedAt(now)
            .expiresAt(expiration)
            .sign();
    }
}
