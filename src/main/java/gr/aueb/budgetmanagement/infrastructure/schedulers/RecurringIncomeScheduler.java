package gr.aueb.budgetmanagement.infrastructure.schedulers;

import java.time.LocalDate;

import org.jboss.logging.Logger;

import gr.aueb.budgetmanagement.application.services.RecurringIncomeService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecurringIncomeScheduler {
    private static final Logger LOG = Logger.getLogger(RecurringIncomeScheduler.class);
    
    private final RecurringIncomeService recurringIncomeService;
    
    public RecurringIncomeScheduler(RecurringIncomeService recurringIncomeService) {
        this.recurringIncomeService = recurringIncomeService;
    }
    
    @Scheduled(cron = "0 0 * * * ?") // Run every hour
    public void executeRecurringIncomeJob() {
        try {
            recurringIncomeService.applyRecurringIncomes(LocalDate.now());
        } catch (Exception e) {
            LOG.error("Error executing recurring income job: {}", e.getMessage(), e);
        }
    }
}
