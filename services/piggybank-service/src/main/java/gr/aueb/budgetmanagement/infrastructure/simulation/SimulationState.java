package gr.aueb.budgetmanagement.infrastructure.simulation;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimulationState {
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    public boolean isHealthy() {
        return healthy.get();
    }

    public void setHealthy(boolean value) {
        healthy.set(value);
    }
}
