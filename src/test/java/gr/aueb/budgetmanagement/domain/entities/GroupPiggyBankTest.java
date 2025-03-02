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

class GroupPiggyBankTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    @Test
    void createGroupPiggyBankShouldEstablishBidirectionalRelationship() {
        // Arrange
        User admin = User.create(
            "admin", 
            "admin@example.com", 
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        Group group = Group.create("testgroup", admin);

        // Act
        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            "Family vacation",
            new Money(BigDecimal.valueOf(1000)),
            ExpenseCategory.ENTERTAINMENT,
            group, 
            group.getAdmin()
        );

        // Assert
        assertSame(group, piggyBank.getGroup(), "PiggyBank should reference the group");
        assertTrue(group.getPiggyBanks().contains(piggyBank), "Group should contain the piggyBank");
        assertEquals(1, group.getPiggyBanks().size(), "Group should have exactly one piggyBank");
    }

    @Test
    void createWithNullGroup() {
        assertThrows(InvalidDomainArgumentException.class, () -> 
            GroupPiggyBank.create(
                "Family vacation", 
                new Money(BigDecimal.valueOf(1000)), 
                ExpenseCategory.ENTERTAINMENT, 
                null,
                null
            ),
            "Creating GroupPiggyBank with null group should throw exception"
        );
    }
}
