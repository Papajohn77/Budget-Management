package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedExpenseRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Expense;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class ExpenseService {
    private final UserRepository userRepository;

    public ExpenseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddedExpenseRepresentation createExpense(@Valid AddExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        Expense expense = user.addExpense(
            command.amount(),
            command.date(),
            command.category()
        );

        userRepository.save(user);

        return new AddedExpenseRepresentation(
            expense.getId(),
            expense.getAmount(),
            expense.getDate(),
            expense.getCategory()
        );
    }
}