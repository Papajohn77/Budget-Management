package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.CreateRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedRecurringExpenseDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.transaction.Transactional;

public class RecurringExpenseService {
    private final UserRepository userRepository;

    public RecurringExpenseService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public CreatedRecurringExpenseDTO createRecurringExpense(CreateRecurringExpenseCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringExpense recurringExpense = RecurringExpense.create(
                command.name(),
                command.amount(),
                command.category(),
                command.startDate(),
                command.endDate(),
                user
        );

        userRepository.save(user);

        return new CreatedRecurringExpenseDTO(
                recurringExpense.getId(),
                recurringExpense.getName(),
                recurringExpense.getAmount(),
                recurringExpense.getCategory(),
                recurringExpense.getStartDate(),
                recurringExpense.getEndDate(),
                recurringExpense.getLastAppliedDate(),
                recurringExpense.isStopped()
        );
    }
}
