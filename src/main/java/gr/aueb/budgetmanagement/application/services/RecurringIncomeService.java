package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedRecurringIncomeDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.transaction.Transactional;

public class RecurringIncomeService {
    private final UserRepository userRepository;

    public RecurringIncomeService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public CreatedRecurringIncomeDTO createRecurringIncome(CreateRecurringIncomeCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringIncome recurringIncome = RecurringIncome.create(
                command.name(),
                command.amount(),
                command.category(),
                command.startDate(),
                command.endDate(),
                user
        );

        userRepository.save(user);

        return new CreatedRecurringIncomeDTO(
                recurringIncome.getId(),
                recurringIncome.getName(),
                recurringIncome.getAmount(),
                recurringIncome.getCategory(),
                recurringIncome.getStartDate(),
                recurringIncome.getEndDate(),
                recurringIncome.getLastAppliedDate(),
                recurringIncome.isStopped()
        );
    }
}
