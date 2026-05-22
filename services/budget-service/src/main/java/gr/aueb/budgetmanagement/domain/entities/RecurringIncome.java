package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
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
@Table(name = "recurring_incomes")
public class RecurringIncome {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurring_income_seq")
    @SequenceGenerator(name = "recurring_income_seq", sequenceName = "recurring_income_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncomeCategory category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "last_applied_date")
    private LocalDate lastAppliedDate;

    @Column(name = "is_stopped", nullable = false)
    private boolean isStopped;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "recurringIncome", cascade = CascadeType.ALL)
    private List<Income> generatedIncomes = new ArrayList<>();

    protected RecurringIncome() {
    }

    public static RecurringIncome create(
        String name,
        Money amount,
        IncomeCategory category,
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
            throw new InvalidDomainArgumentException("End Date cannot be before End Date");
        }

        if (user == null) {
            throw new InvalidDomainArgumentException("User cannot be null");
        }

        RecurringIncome recurringIncome = new RecurringIncome();
        recurringIncome.name = name;
        recurringIncome.amount = amount;
        recurringIncome.category = category;
        recurringIncome.startDate = startDate;
        recurringIncome.endDate = endDate;
        recurringIncome.user = user;
        return recurringIncome;
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

    public IncomeCategory getCategory() {
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

    public List<Income> getGeneratedIncomes() {
        return Collections.unmodifiableList(generatedIncomes);
    }

    public User getUser() {
        return user;
    }

    public void stop(boolean isStopped) {
        if (!isStopped) {
            throw new InvalidDomainArgumentException("Cannot restart recurring income");
        }
        this.isStopped = true;
    }

    public Income apply(LocalDate currentDate) {
        if (!shouldApply(currentDate)) {
            return null;
        }

        LocalDate applicationDate = lastAppliedDate == null 
            ? startDate 
            : lastAppliedDate.plusMonths(1);

        Income income = Income.create(
            amount,
            applicationDate,
            category,
            user
        );

        income.addRecurringIncome(this);
        generatedIncomes.add(income);

        lastAppliedDate = applicationDate;

        return income;
    }

    public boolean shouldApply(LocalDate currentDate) {
        if (isStopped) {
            return false;
        }

        if (lastAppliedDate == null) {
            return !currentDate.isBefore(startDate);
        }

        long totalMonthsPermitted = ChronoUnit.MONTHS.between(startDate, endDate);
        if (startDate.plusMonths(totalMonthsPermitted).isBefore(endDate)) {
            totalMonthsPermitted++;
        }

        if (generatedIncomes.size() >= totalMonthsPermitted) {
            return false;
        }

        LocalDate nextApplicationDate = lastAppliedDate.plusMonths(1);
        return !nextApplicationDate.isAfter(currentDate);
    }
}
