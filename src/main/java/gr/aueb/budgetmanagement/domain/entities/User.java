package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.InvitationAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.UnauthorizedOperationException;
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

    @ManyToMany(mappedBy = "members", cascade = CascadeType.ALL)
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecurringExpense> recurringExpenses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Income> incomes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecurringIncome> recurringIncomes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonalPiggyBank> piggyBanks = new HashSet<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Invitation> invitations = new HashSet<>();
    
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

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public boolean verifyPassword(String rawPassword, PasswordHasher passwordHasher) {
        return passwordHasher.verifyPassword(rawPassword, this.password);
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

    public SavingsOperation allocateSavings(Money amount, LocalDate date) {
        return savings.allocate(amount, date);
    }

    public SavingsOperation deallocateSavings(Money amount, LocalDate date) {
        return savings.deallocate(amount, date);
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public Set<Expense> getExpenses() {
        return Collections.unmodifiableSet(expenses);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public Set<RecurringExpense> getRecurringExpenses() {
        return Collections.unmodifiableSet(recurringExpenses);
    }

    public void addRecurringExpense(RecurringExpense recurringExpense) {
        recurringExpenses.add(recurringExpense);
    }

    public Set<Income> getIncomes() {
        return Collections.unmodifiableSet(incomes);
    }

    public void addIncome(Income income) {
        incomes.add(income);
    }

    public Set<RecurringIncome> getRecurringIncomes() {
        return Collections.unmodifiableSet(recurringIncomes);
    }

    public void addRecurringIncome(RecurringIncome recurringIncome) {
        recurringIncomes.add(recurringIncome);
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

    public Set<Invitation> getInvitations() {
        return Collections.unmodifiableSet(invitations);
    }

    void addInvitation(Invitation invitation) {
        if (invitation == null) {
            throw new InvalidDomainArgumentException("Invitation cannot be null");
        }
        invitations.add(invitation);
    }

    private boolean hasAlreadyBeenInvitedTo(Group group) {
        return invitations.stream()
            .anyMatch(invitation -> invitation.getGroup().equals(group));
    }

    public Invitation sendInvitationTo(User invitee, Group group) {
        if (group.getAdmin() != this) {
            throw new UnauthorizedOperationException("Only the group admin can send invitations");
        }
        if (invitee.hasAlreadyBeenInvitedTo(group)) {
            throw new InvitationAlreadyExistsException("Invitation already exists");
        }
        return Invitation.create(group, invitee);
    }

    public Invitation respondToInvitation(
        Invitation invitation, 
        InvitationResponseOperationType operationType
    ) {
        if (!invitations.contains(invitation)) {
            throw new UnauthorizedOperationException("User cannot respond to invitation");
        }
        switch (operationType) {
            case ACCEPT -> invitation.accept();
            case REJECT -> invitation.reject();
        }
        return invitation;
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