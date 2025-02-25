package gr.aueb.budgetmanagement.domain.entities;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "incomes")
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "income_seq")
    @SequenceGenerator(name = "income_seq", sequenceName = "income_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "recurring_income_id")
    private RecurringIncome recurringIncome;

    @Column(nullable = false)
    private Money amount;

    @Column(name = "date",nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncomeCategory category;

    protected Income() {}

    public static Income create(User user, Money amount, LocalDate date, IncomeCategory category) {

        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }

        if (category == null) {
            throw new InvalidDomainArgumentException("Category cannot be null");
        }

        if (date == null) {
            throw new InvalidDomainArgumentException("Date cannot be null");
        }

        Income income = new Income();
        income.user = user;
        income.amount = amount;
        income.date = date;
        income.category = category;

        user.addIncome(income);
        return income;
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
    public LocalDate getDate() {return date;}
    public IncomeCategory getCategory() {
        return category;
    }
}