package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "piggy_bank_allocations")
public class PiggyBankAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "allocation_seq")
    @SequenceGenerator(name = "allocation_seq", sequenceName = "allocation_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Money amount;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piggy_bank_id", nullable = false)
    private PiggyBank piggyBank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected PiggyBankAllocation() {

    }

    public static PiggyBankAllocation create(
        Money amount, 
        LocalDate date, 
        PiggyBank piggyBank, 
        User user
    ) {
        if (amount == null) {
            throw new InvalidDomainArgumentException("Amount cannot be null");
        }

        if (date == null) {
            throw new InvalidDomainArgumentException("Date cannot be null");
        }

        if (piggyBank == null) {
            throw new InvalidDomainArgumentException("PiggyBank cannot be null");
        }

        if (user == null) {
            throw new InvalidDomainArgumentException("User cannot be null");
        }

        PiggyBankAllocation allocation = new PiggyBankAllocation();
        allocation.amount = amount;
        allocation.date = date;
        allocation.piggyBank = piggyBank;
        allocation.user = user;
        return allocation;
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

    public PiggyBank getPiggyBank() {
        return piggyBank;
    }

    public User getUser() {
        return user;
    }
}
