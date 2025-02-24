package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.dto.AddExpenseDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Expense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.transaction.Transactional;

public class ExpenseService {
    private final UserRepository userRepository;

    public ExpenseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddExpenseDTO createExpense(AddExpenseCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Expense expense = Expense.create(
                user,
                command.amount(),
                command.date(),
                command.category()
        );

        userRepository.save(user);

        return new AddExpenseDTO(
                expense.getId(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory()
        );
    }
}