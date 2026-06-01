package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(1L);
    }

    @Test
    void addPiggyBankShouldAddToUser() {
        PersonalPiggyBank piggyBank = createPersonalPiggyBank(user);

        assertTrue(user.getPiggyBanks().contains(piggyBank));
        assertEquals(1, user.getPiggyBanks().size());
    }

    @Test
    void addPiggyBankWithNullPiggyBank() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> user.addPiggyBank(null)
        );
    }

    @Test
    void addPiggyBankTwiceShouldNotDuplicate() {
        // Arrange
        PersonalPiggyBank piggyBank = createPersonalPiggyBank(user);

        // Act
        user.addPiggyBank(piggyBank); // Adding again

        // Assert
        assertEquals(1, user.getPiggyBanks().size());
    }

    @Test
    void getPiggyBanksShouldReturnUnmodifiableSet() {
        // Arrange
        Set<PersonalPiggyBank> piggyBanks = user.getPiggyBanks();

        // Act & Assert
        assertThrows(
            UnsupportedOperationException.class,
            () -> piggyBanks.add(new PersonalPiggyBank())
        );
    }

    @Test
    void addNullInvitation() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> user.addInvitation(null)
        );
    }

    @Test
    void equalsWithNullObject() {
        assertNotEquals(user, null);
    }

    @Test
    void equalsAndHashCode_equalUsers() {
        User user1 = User.create(1L);
        User user2 = User.create(1L);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equalsAndHashCode_notEqualUsers() {
        User user1 = User.create(1L);
        User user2 = User.create(2L);

        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    private PersonalPiggyBank createPersonalPiggyBank(User user) {
        return PersonalPiggyBank.create(
            "Test Piggy Bank",
            new Money(new BigDecimal("100.00")),
            ExpenseCategory.OTHER,
            user
        );
    }
}
