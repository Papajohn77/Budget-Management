package gr.aueb.budgetmanagement.application.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.budgetmanagement.application.commands.AddRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.RecurringExpenseRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringExpenseRepresentation;
import gr.aueb.budgetmanagement.application.representations.RecurringExpensesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Expense;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class RecurringExpenseService {
    private final UserRepository userRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;

    public RecurringExpenseService(
        UserRepository userRepository,
        RecurringExpenseRepository recurringExpenseRepository
    ) {
        this.userRepository = userRepository;
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    @Transactional
    public AddedRecurringExpenseRepresentation createRecurringExpense(@Valid AddRecurringExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        RecurringExpense recurringExpense = user.addRecurringExpense(
            command.name(),
            command.amount(),
            command.category(),
            command.startDate(),
            command.endDate()
        );

        userRepository.save(user);

        return toAddedRecurringExpenseRepresentation(recurringExpense);
    }

    @Transactional
    public void updateRecurringExpense(@Valid UpdateRecurringExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        RecurringExpense recurringExpense = user.getRecurringExpenses().stream()
            .filter(re -> re.getId().equals(command.recurringExpenseId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Recurring expense with id: " + command.recurringExpenseId() 
                + " was not found for user with id: " + command.userId()
            ));

        recurringExpense.stop(command.isStopped());

        userRepository.save(user);
    }

    @Transactional
    public void deleteRecurringExpense(Long recurringExpenseId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        user.getRecurringExpenses().stream()
            .filter(re -> re.getId().equals(recurringExpenseId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Recurring expense with id: " + recurringExpenseId 
                + " was not found for user with id: " + userId
            ));

        user.removeRecurringExpense(recurringExpenseId);

        userRepository.save(user);
    }

    @Transactional
    public RecurringExpensesRepresentation getRecurringExpenses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<RecurringExpense> recurringExpenses = new ArrayList<>(user.getRecurringExpenses());

        return new RecurringExpensesRepresentation(
            toAddedRecurringExpenseRepresentationList(recurringExpenses)
        );
    }

    @Transactional
    public void applyRecurringExpenses(LocalDate currentDate) {
        List<RecurringExpense> activeRecurringExpenses = recurringExpenseRepository.findNonStoppedRecurringExpenses();
        for (RecurringExpense recurringExpense : activeRecurringExpenses) {
            Expense generatedExpense = recurringExpense.apply(currentDate);
            if (generatedExpense != null) {
                recurringExpenseRepository.save(recurringExpense);
            }
        }
    }

    private List<AddedRecurringExpenseRepresentation> toAddedRecurringExpenseRepresentationList(List<RecurringExpense> recurringExpenses) {
        return recurringExpenses.stream()
            .map(this::toAddedRecurringExpenseRepresentation)
            .toList();
    }

    private AddedRecurringExpenseRepresentation toAddedRecurringExpenseRepresentation(RecurringExpense recurringExpense) {
        return new AddedRecurringExpenseRepresentation(
            recurringExpense.getId(),
            recurringExpense.getName(),
            recurringExpense.getAmount().getValue(),
            recurringExpense.getCategory(),
            recurringExpense.getStartDate(),
            recurringExpense.getEndDate(),
            recurringExpense.getLastAppliedDate(),
            recurringExpense.isStopped()
        );
    }


}