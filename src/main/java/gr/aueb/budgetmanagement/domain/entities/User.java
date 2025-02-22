package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Savings savings;

    @ManyToMany(mappedBy = "members")
    private Set<Group> groups = new HashSet<>();

    protected User() {

    }

    public static User create(String username, EmailAddress email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidDomainArgumentException("Username cannot be null or empty");
        }

        if (email == null) {
            throw new InvalidDomainArgumentException("Email cannot be null");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new InvalidDomainArgumentException("Password cannot be null or empty");
        }

        User user = new User();
        user.username = username;
        user.email = email;
        user.password = password;
        Savings.createFor(user);
        return user;
    }

    public SavingsOperation allocateSavings(Money amount, LocalDate date) {
        return savings.allocate(amount, date);
    }

    public SavingsOperation deallocateSavings(Money amount, LocalDate date) {
        return savings.deallocate(amount, date);
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