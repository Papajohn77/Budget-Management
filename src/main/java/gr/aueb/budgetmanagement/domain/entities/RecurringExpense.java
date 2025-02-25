package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurring_expense_seq")
    @SequenceGenerator(name = "recurring_expense_seq", sequenceName = "recurring_expense_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "last_applied_date")
    private LocalDate lastAppliedDate;

    @Column(name = "is_stopped", nullable = false)
    private boolean isStopped;

    @ManyToOne
    @JoinColumn(name = "recurring_expense_id")
    private RecurringExpense recurringExpense;

    @OneToMany(mappedBy = "recurringExpense", cascade = CascadeType.ALL)
    private List<Expense> generatedExpenses = new ArrayList<>();

    protected RecurringExpense() {
    }

    public static RecurringExpense create(
            String name,
            Money amount,
            ExpenseCategory category,
            LocalDate startDate,
            LocalDate endDate,
            User user
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainArgumentException("Name cannot be null");
        }
        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }
        if (category == null) {
            throw new InvalidDomainArgumentException("Category cannot be null");
        }
        if (startDate == null) {
            throw new InvalidDomainArgumentException("Start Date cannot be null");
        }
        if (endDate == null) {
            throw new InvalidDomainArgumentException("End Date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.name = name;
        recurringExpense.amount = amount;
        recurringExpense.category = category;
        recurringExpense.startDate = startDate;
        recurringExpense.endDate = endDate;
        recurringExpense.user = user;
        user.addRecurringExpense(recurringExpense);
        return recurringExpense;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getAmount() {
        return amount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getLastAppliedDate() {
        return lastAppliedDate;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public List<Expense> getGeneratedExpenses() {
        return Collections.unmodifiableList(generatedExpenses);
    }

    public User getUser() {
        return user;
    }
}