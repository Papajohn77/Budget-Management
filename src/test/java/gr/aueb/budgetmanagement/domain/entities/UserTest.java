package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;

class UserTest {
    @Test
    void shouldNotAllowMultipleSavingsAccounts() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail(new EmailAddress("test@example.com"));
        user.setPassword("hashedPassword");
        
        Savings.createFor(user);

        // Act & Assert
        assertThrows(
            SavingsAlreadyExistsException.class, 
            () -> Savings.createFor(user)
        );
    }
}
