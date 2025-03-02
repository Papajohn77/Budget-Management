package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.dto.AddedRecurringExpenseDTO;
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
    public AddedRecurringExpenseDTO createRecurringExpense(AddRecurringExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringExpense recurringExpense = user.addRecurringExpense(
            command.name(),
            command.amount(),
            command.category(),
            command.startDate(),
            command.endDate()
        );

        userRepository.save(user);

        return new AddedRecurringExpenseDTO(
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
