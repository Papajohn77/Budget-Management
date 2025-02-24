package gr.aueb.budgetmanagement.domain.entities;

import java.awt.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.exceptions.GroupAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.domain.valueobjects.Password;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private EmailAddress email;

    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Savings savings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecurringExpense> recurringExpenses = new HashSet<>();

    @ManyToMany(mappedBy = "members", cascade = CascadeType.ALL)
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonalPiggyBank> piggyBanks = new HashSet<>();

    protected User() {

    }

    public static User create(String username, String email, String rawPassword, PasswordHasher passwordHasher) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidDomainArgumentException("Username cannot be null or empty");
        }
        EmailAddress emailAddress = new EmailAddress(email);
        Password password = new Password(rawPassword);
        String hashedPassword = passwordHasher.hashPassword(password.getValue());

        User user = new User();
        user.username = username;
        user.email = emailAddress;
        user.password = hashedPassword;
        user.savings = Savings.create(user);
        return user;
    }

    public boolean verifyPassword(String rawPassword, PasswordHasher passwordHasher) {
        return passwordHasher.verifyPassword(rawPassword, this.password);
    }

    public SavingsOperation allocateSavings(Money amount, LocalDate date) {
        return savings.allocate(amount, date);
    }

    public SavingsOperation deallocateSavings(Money amount, LocalDate date) {
        return savings.deallocate(amount, date);
    }

    public Group createGroup(String name) {
        boolean groupAlreadyExists = groups.stream()
            .anyMatch(group -> group.getName().equals(name));

        if (groupAlreadyExists) {
            throw new GroupAlreadyExistsException("Group with name '" + name + "' already exists for this user");
        }

        Group group = Group.create(name, this);
        groups.add(group);
        return group;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Savings getSavings() {
        return savings;
    }

    void setSavings(Savings savings) {
        if (this.savings != null) {
            throw new SavingsAlreadyExistsException("User already has a savings account");
        }
        this.savings = savings;
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public Set<PersonalPiggyBank> getPiggyBanks() {
        return Collections.unmodifiableSet(piggyBanks);
    }

    void addPiggyBank(PersonalPiggyBank piggyBank) {
        if (piggyBank == null) {
            throw new InvalidDomainArgumentException("PiggyBank cannot be null");
        }
        piggyBanks.add(piggyBank);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void addRecurringExpense(RecurringExpense recurringExpense) {
        recurringExpenses.add(recurringExpense);
    }

    public Set<Expense> getExpenses() {
        return Collections.unmodifiableSet(expenses);
    }

    public Set<RecurringExpense> getRecurringExpenses() {
        return Collections.unmodifiableSet(recurringExpenses);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        
        User user = (User) other;
        return Objects.equals(username, user.username) && 
            Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}