package gr.aueb.budgetmanagement.domain.entities;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expense_seq")
    @SequenceGenerator(name = "expense_seq", sequenceName = "expense_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recurring_expense_id")
    private RecurringExpense recurringExpense;

    protected Expense() {

    }

    public static Expense create(
        Money amount, 
        LocalDate date, 
        ExpenseCategory category, 
        User user
    ) {
        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }

        if (date == null) {
            throw new InvalidDomainArgumentException("Date cannot be null");
        }

        if (category == null) {
            throw new InvalidDomainArgumentException("Category cannot be null");
        }

        if (user == null) {
            throw new InvalidDomainArgumentException("User cannot be null");
        }

        Expense expense = new Expense();
        expense.amount = amount;
        expense.date = date;
        expense.category = category;
        expense.user = user;
        return expense;
    }

    public Long getId() {
        return id;
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

    public User getUser() {
        return user;
    }
}