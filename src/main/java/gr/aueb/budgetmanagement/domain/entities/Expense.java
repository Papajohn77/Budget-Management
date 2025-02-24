package gr.aueb.budgetmanagement.domain.entities;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.converters.MoneyConverter;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expense_seq")
    @SequenceGenerator(name = "expense_seq", sequenceName = "expense_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recurring_expense_id")
    private RecurringExpense recurringExpense;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    protected Expense() {}

    public static Expense create(User user, Money amount, LocalDate date, ExpenseCategory category) {

        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }

        if (category == null) {
            throw new InvalidDomainArgumentException("Category cannot be null");
        }

        if (date == null) {
            throw new InvalidDomainArgumentException("Date cannot be null");
        }

        Expense expense = new Expense();
        expense.user = user;
        expense.amount = amount;
        expense.date = date;
        expense.category = category;

        user.addExpense(expense);
        return expense;
    }

    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Money getAmount() {
        return amount;
    }
    public LocalDate getDate() {
        return date;
    }
    public ExpenseCategory getCategory() {
        return category;
    }
}