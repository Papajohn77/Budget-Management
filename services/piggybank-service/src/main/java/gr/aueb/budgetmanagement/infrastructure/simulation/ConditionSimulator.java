package gr.aueb.budgetmanagement.infrastructure.simulation;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConditionSimulator {
    private final AtomicReference<SimulatedCondition> current = new AtomicReference<>(new HealthyCondition());

    public void useStrategy(SimulatedConditionType strategy, Integer delaySeconds) {
        current.set(switch (strategy) {
            case HEALTHY -> new HealthyCondition();
            case SLOW -> delaySeconds == null
                ? new SlowCondition()
                : new SlowCondition(Duration.ofSeconds(delaySeconds));
            case FAILING -> new FailingCondition();
        });
    }

    public void simulate() {
        SimulatedCondition simulatedCondition = current.get();
        simulatedCondition.apply();
    }
}
