package gr.aueb.budgetmanagement.domain.entities;

import java.util.Objects;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Password;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    
    protected User() {

    }

    public static User create(
        String username, 
        String email, 
        String rawPassword, 
        PasswordHasher passwordHasher
    ) {
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
