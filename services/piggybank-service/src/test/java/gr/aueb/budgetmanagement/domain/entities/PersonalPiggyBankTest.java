package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class PersonalPiggyBankTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    @Test
    void createPersonalPiggyBankShouldEstablishBidirectionalRelationship() {
        // Arrange
        User user = User.create(
            "testuser", 
            "test@example.com", 
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        // Act
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Vacation fund",
            new Money(new BigDecimal("1000.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );

        // Assert
        assertSame(user, piggyBank.getUser(), "PiggyBank should reference the user");
        assertTrue(user.getPiggyBanks().contains(piggyBank), "User should contain the piggyBank");
        assertEquals(1, user.getPiggyBanks().size(), "User should have exactly one piggyBank");
    }

    @Test
    void createWithNullUser() {
        assertThrows(InvalidDomainArgumentException.class, () -> 
            PersonalPiggyBank.create(
                "Vacation fund", 
                new Money(BigDecimal.valueOf(1000)), 
                ExpenseCategory.ENTERTAINMENT, 
                null
            ),
            "Creating PersonalPiggyBank with null user should throw exception"
        );
    }
}
