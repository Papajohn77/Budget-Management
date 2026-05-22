package gr.aueb.budgetmanagement.application.services;

import java.time.LocalDate;
import java.util.List;

import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.ExpenseRepresentation;
import gr.aueb.budgetmanagement.application.representations.ExpensesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Expense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
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
    public ExpenseRepresentation createExpense(@Valid AddExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        Expense expense = user.addExpense(
            command.amount(),
            command.date(),
            command.category()
        );

        userRepository.save(user);

        return new ExpenseRepresentation(
            expense.getId(),
            expense.getAmount().getValue(),
            expense.getDate(),
            expense.getCategory()
        );
    }

    @Transactional
    public ExpenseRepresentation updateExpense(@Valid UpdateExpenseCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        Expense expense = user.getExpenses().stream()
            .filter(e -> e.getId().equals(command.expenseId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Expense not found with id: " + command.expenseId()));

        expense.update(command.amount(), command.date(), command.category());

        userRepository.save(user);

        return new ExpenseRepresentation(
            expense.getId(),
            expense.getAmount().getValue(),
            expense.getDate(),
            expense.getCategory()
        );
    }

    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        user.removeExpense(expenseId);

        userRepository.save(user);
    }

    public ExpensesRepresentation getExpenses(Long userId, LocalDate fromDate, LocalDate toDate, ExpenseCategory category) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Expense> expenses = user.getExpenses().stream()
            .filter(e -> fromDate == null || !e.getDate().isBefore(fromDate))
            .filter(e -> toDate == null || !e.getDate().isAfter(toDate))
            .filter(e -> category == null || e.getCategory() == category)
            .toList();

        return new ExpensesRepresentation(toAddedExpenseRepresentationList(expenses));
    }

    private List<ExpenseRepresentation> toAddedExpenseRepresentationList(List<Expense> expenses) {
        return expenses.stream()
            .map(this::toAddedExpenseRepresentation)
            .toList();
    }

    private ExpenseRepresentation toAddedExpenseRepresentation(Expense expense) {
        return new ExpenseRepresentation(
            expense.getId(),
            expense.getAmount().getValue(),
            expense.getDate(),
            expense.getCategory()
        );
    }
}