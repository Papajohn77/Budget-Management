package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class RecurringIncomeService {
    private final UserRepository userRepository;

    public RecurringIncomeService(
        UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddedRecurringIncomeRepresentation createRecurringIncome(
        @Valid AddRecurringIncomeCommand command
    ) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringIncome recurringIncome = user.addRecurringIncome(
            command.name(),
            command.amount(),
            command.category(),
            command.startDate(),
            command.endDate()
        );

        userRepository.save(user);

        return new AddedRecurringIncomeRepresentation(
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
