package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "savings_operations")
public class SavingsOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "savings_operation_seq")
    @SequenceGenerator(name = "savings_operation_seq", sequenceName = "savings_operation_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SavingsOperationType operation;

    @ManyToOne
    @JoinColumn(name = "savings_id", nullable = false)
    private Savings savings;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected SavingsOperation() {

    }

    public static SavingsOperation create(
        Money amount, 
        LocalDate date, 
        SavingsOperationType operation,
        Savings savings
    ) {
        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }

        if (date == null) {
            throw new InvalidDomainArgumentException("Date cannot be null");
        }

        if (operation == null) {
            throw new InvalidDomainArgumentException("Operation type cannot be null");
        }

        if (savings == null) {
            throw new InvalidDomainArgumentException("Savings cannot be null");
        }

        SavingsOperation savingsOperation = new SavingsOperation();
        savingsOperation.amount = amount;
        savingsOperation.date = date;
        savingsOperation.operation = operation;
        savingsOperation.savings = savings;
        savingsOperation.user = savings.getUser();
        savings.addOperation(savingsOperation);
        return savingsOperation;
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

    public SavingsOperationType getOperation() {
        return operation;
    }

    public Savings getSavings() {
        return savings;
    }

    public User getUser() {
        return user;
    }
}
