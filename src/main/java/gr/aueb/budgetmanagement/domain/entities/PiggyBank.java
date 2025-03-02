package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "piggy_banks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class PiggyBank {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "piggy_bank_seq")
    @SequenceGenerator(name = "piggy_bank_seq", sequenceName = "piggy_bank_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount", nullable = false)
    private Money targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @OneToMany(mappedBy = "piggyBank", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PiggyBankAllocation> allocations = new HashSet<>();

    protected PiggyBank() {

    }

    protected PiggyBank(String name, Money targetAmount, ExpenseCategory category) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDomainArgumentException("Name cannot be null or empty");
        }

        if (targetAmount == null) {
            throw new InvalidDomainArgumentException("Target amount cannot be null");
        }

        if (category == null) {
            throw new InvalidDomainArgumentException("Category cannot be null");
        }

        this.name = name;
        this.targetAmount = targetAmount;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getTargetAmount() {
        return targetAmount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public Set<PiggyBankAllocation> getAllocations() {
        return Collections.unmodifiableSet(allocations);
    }

    public PiggyBankAllocation allocate(
        Money amount, 
        LocalDate date, 
        User user
    ) {
        if (!isAuthorizedUser(user)) {
            throw new UnsupportedOperationException("User is not authorized to access this piggy bank");
        }

        PiggyBankAllocation allocation = PiggyBankAllocation.create(
            amount, 
            date, 
            this, 
            user
        );
        allocations.add(allocation);
        return allocation;
    }

    public abstract boolean isAuthorizedUser(User user);
    public abstract boolean canBeDissolvedBy(User user);
}
