package gr.aueb.budgetmanagement.infrastructure.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import io.smallrye.jwt.util.KeyUtils;
import jakarta.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class JwtKeysHealthCheck implements HealthCheck {

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String verificationKeyLocation;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("JWT keys");
        try {
            KeyUtils.readPublicKey(verificationKeyLocation);
            return builder
                .up()
                .build();
        } catch (Exception e) {
            return builder
                .withData("error", e.getMessage())
                .down()
                .build();
        }
    }
}
