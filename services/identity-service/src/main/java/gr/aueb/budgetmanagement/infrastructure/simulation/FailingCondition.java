package gr.aueb.budgetmanagement.infrastructure.simulation;

import jakarta.ws.rs.ServiceUnavailableException;

public final class FailingCondition implements SimulatedCondition {

    @Override
    public void apply() {
        throw new ServiceUnavailableException("Identity service is simulating a failure");
    }
}
