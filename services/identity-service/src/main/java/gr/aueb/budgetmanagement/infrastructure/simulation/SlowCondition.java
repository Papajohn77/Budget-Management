package gr.aueb.budgetmanagement.infrastructure.simulation;

import java.time.Duration;

public final class SlowCondition implements SimulatedCondition {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(5);

    private final Duration delay;

    public SlowCondition() {
        this(DEFAULT_DELAY);
    }

    public SlowCondition(Duration delay) {
        this.delay = delay;
    }

    @Override
    public void apply() {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
