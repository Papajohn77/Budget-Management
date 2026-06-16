package gr.aueb.budgetmanagement.infrastructure.simulation;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;

@ApplicationScoped
public class HealthSimulatorProducer {

    @ConfigProperty(name = "health.simulator", defaultValue = "healthy")
    String strategy;

    @Inject
    SimulationState state;

    @Produces
    @ApplicationScoped
    public HealthSimulator produce() {
        return switch (strategy) {
            case "delay" -> () -> {
                if (!state.isHealthy()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            case "failing" -> () -> {
                if (!state.isHealthy()) {
                    throw new ServiceUnavailableException("Piggy bank service is simulating a failure");
                }
            };
            default -> () -> {};
        };
    }
}
