package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class PiggyBankTest {
    private static final String VALID_NAME = "My Piggy Bank";
    private static final Money VALID_TARGET = new Money(BigDecimal.valueOf(1000));
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.OTHER;

    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        user = User.create(
            "testuser", 
            new EmailAddress("test@example.com"), 
            "hashedPassword123"
        );
        group = Group.create("testgroup", user);
    }

    @Nested
    class PersonalPiggyBankTest {
        @Test
        void createWithValidData() {
            // Act
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );

            // Assert
            assertNotNull(piggyBank);
            assertEquals(VALID_NAME, piggyBank.getName());
            assertEquals(VALID_TARGET, piggyBank.getTargetAmount());
            assertEquals(VALID_CATEGORY, piggyBank.getCategory());
            assertEquals(user, piggyBank.getUser());
            assertTrue(user.getPiggyBanks().contains(piggyBank));
        }

        @Test
        void createWithNullName() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                PersonalPiggyBank.create(
                    null,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    user
                )
            );
        }

        @Test
        void createWithEmptyName() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                PersonalPiggyBank.create(
                    "",
                    VALID_TARGET,
                    VALID_CATEGORY,
                    user
                )
            );
        }

        @Test
        void createWithNullTargetAmount() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                PersonalPiggyBank.create(
                    VALID_NAME,
                    null,
                    VALID_CATEGORY,
                    user
                )
            );
        }

        @Test
        void createWithNullCategory() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                PersonalPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    null,
                    user
                )
            );
        }

        @Test
        void createWithNullUser() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                PersonalPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    null
                )
            );
        }
    }

    @Nested
    class GroupPiggyBankTest {
        @Test
        void createWithValidData() {
            // Act
            GroupPiggyBank piggyBank = GroupPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                group
            );

            // Assert
            assertNotNull(piggyBank);
            assertEquals(VALID_NAME, piggyBank.getName());
            assertEquals(VALID_TARGET, piggyBank.getTargetAmount());
            assertEquals(VALID_CATEGORY, piggyBank.getCategory());
            assertEquals(group, piggyBank.getGroup());
            assertTrue(group.getPiggyBanks().contains(piggyBank));
        }

        @Test
        void createWithNullName() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                GroupPiggyBank.create(
                    null,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    group
                )
            );
        }

        @Test
        void createWithEmptyName() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                GroupPiggyBank.create(
                    "",
                    VALID_TARGET,
                    VALID_CATEGORY,
                    group
                )
            );
        }

        @Test
        void createWithNullTargetAmount() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                GroupPiggyBank.create(
                    VALID_NAME,
                    null,
                    VALID_CATEGORY,
                    group
                )
            );
        }

        @Test
        void createWithNullCategory() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                GroupPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    null,
                    group
                )
            );
        }

        @Test
        void create_WithNullGroup_ShouldThrowException() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () -> 
                GroupPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    null
                )
            );
        }
    }
}
