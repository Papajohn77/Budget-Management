package gr.aueb.budgetmanagement.infrastructure.schedulers;

import java.time.LocalDate;

import org.jboss.logging.Logger;

import gr.aueb.budgetmanagement.application.services.RecurringExpenseService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecurringExpenseScheduler {
    private static final Logger LOG = Logger.getLogger(RecurringExpenseScheduler.class);
    
    private final RecurringExpenseService recurringExpenseService;
    
    public RecurringExpenseScheduler(RecurringExpenseService recurringExpenseService) {
        this.recurringExpenseService = recurringExpenseService;
    }
    
    @Scheduled(cron = "0 0 * * * ?") // Run every hour
    public void executeRecurringExpenseJob() {
        try {
            recurringExpenseService.applyRecurringExpenses(LocalDate.now());
        } catch (Exception e) {
            LOG.error("Error executing recurring expense job: {}", e.getMessage(), e);
        }
    }
}
