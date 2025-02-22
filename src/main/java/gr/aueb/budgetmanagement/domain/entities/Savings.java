package gr.aueb.budgetmanagement.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "savings")
public class Savings {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "savings_seq")
    @SequenceGenerator(name = "savings_seq", sequenceName = "savings_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "savings", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingsOperation> operations = new ArrayList<>();

    protected Savings() {

    }

    public static Savings createFor(User user) {
        Savings savings = new Savings();
        savings.user = user;
        user.setSavings(savings);
        return savings;
    }

    public SavingsOperation allocate(Money amount, LocalDate date) {        
        return SavingsOperation.create(amount, date, SavingsOperationType.ALLOCATION, this);
    }

    public SavingsOperation deallocate(Money amount, LocalDate date) {
        BigDecimal newAmount = getCurrentAmount().getValue().subtract(amount.getValue());
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientSavingsException("Insufficient savings balance");
        }
        
        return SavingsOperation.create(amount, date, SavingsOperationType.DEALLOCATION, this);
    }

    void addOperation(SavingsOperation operation) {
        operations.add(operation);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Money getCurrentAmount() {
        BigDecimal total = operations.stream()
            .map(operation -> operation.getOperation() == SavingsOperationType.ALLOCATION
                    ? operation.getAmount().getValue()
                    : operation.getAmount().getValue().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Money(total);
    }

    public List<SavingsOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }
}
