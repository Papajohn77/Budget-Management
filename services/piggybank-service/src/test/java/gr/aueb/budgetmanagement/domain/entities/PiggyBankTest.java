package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class PiggyBankTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String VALID_NAME = "My Piggy Bank";
    private static final Money VALID_TARGET = new Money(BigDecimal.valueOf(1000));
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.OTHER;

    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        user = User.create(
            "testuser", 
            "test@example.com", 
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
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
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> PersonalPiggyBank.create(
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
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> PersonalPiggyBank.create(
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
            assertThrows(InvalidDomainArgumentException.class, 
            () -> PersonalPiggyBank.create(
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
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> PersonalPiggyBank.create(
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
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> PersonalPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    null
                )
            );
        }

        @Test
        void personalPiggyBankShouldAuthorizeOwner() {
            // Arrange
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );

            // Assert
            assertTrue(piggyBank.isAuthorizedUser(user));
        }

        @Test
        void personalPiggyBankShouldNotAuthorizeOtherUser() {
            // Arrange
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );
            User otherUser = User.create(
                "otheruser",
                "other@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );

            // Assert
            assertFalse(piggyBank.isAuthorizedUser(otherUser));
        }

        @Test
        void personalPiggyBankShouldBeDissolvedByOwner() {
            // Arrange
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );

            // Assert
            assertTrue(piggyBank.canBeDissolvedBy(user));
        }

        @Test
        void personalPiggyBankShouldNotBeDissolvedByOtherUser() {
            // Arrange
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );
            User otherUser = User.create(
                "otheruser",
                "other@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );

            // Assert
            assertFalse(piggyBank.canBeDissolvedBy(otherUser));
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
                group, 
                group.getAdmin()
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
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> GroupPiggyBank.create(
                    null,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    group, 
                    group.getAdmin()
                )
            );
        }

        @Test
        void createWithEmptyName() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> GroupPiggyBank.create(
                    "",
                    VALID_TARGET,
                    VALID_CATEGORY,
                    group, 
                    group.getAdmin()
                )
            );
        }

        @Test
        void createWithNullTargetAmount() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> GroupPiggyBank.create(
                    VALID_NAME,
                    null,
                    VALID_CATEGORY,
                    group,
                    group.getAdmin()
                )
            );
        }

        @Test
        void createWithNullCategory() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> GroupPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    null,
                    group, 
                    group.getAdmin()
                )
            );
        }

        @Test
        void createWithNullGroup() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> GroupPiggyBank.create(
                    VALID_NAME,
                    VALID_TARGET,
                    VALID_CATEGORY,
                    null, 
                    null
                )
            );
        }

        @Test
        void groupPiggyBankShouldAuthorizeGroupMember() {
            // Arrange
            GroupPiggyBank piggyBank = GroupPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                group, 
                group.getAdmin()
            );

            // Assert
            assertTrue(piggyBank.isAuthorizedUser(user));
        }

        @Test
        void groupPiggyBankShouldNotAuthorizeNonMember() {
            // Arrange
            GroupPiggyBank piggyBank = GroupPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                group, 
                group.getAdmin()
            );
            User nonMember = User.create(
                "nonmember",
                "nonmember@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );

            // Assert
            assertFalse(piggyBank.isAuthorizedUser(nonMember));
        }

        @Test
        void groupPiggyBankShouldBeDissolvedByAdmin() {
            // Arrange
            GroupPiggyBank piggyBank = GroupPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                group, 
                group.getAdmin()
            );

            // Assert
            assertTrue(piggyBank.canBeDissolvedBy(user));
        }

        @Test
        void groupPiggyBankShouldNotBeDissolvedByMember() {
            // Arrange
            GroupPiggyBank piggyBank = GroupPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                group,
                group.getAdmin()
            );
            User member = User.create(
                "member",
                "member@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );
            group.addMember(member);

            // Assert
            assertFalse(piggyBank.canBeDissolvedBy(member));
        }
    }

    @Nested
    class PiggyBankAllocationTest {
        @Test
        void addAllocationShouldMaintainBidirectionalRelationship() {
            // Arrange
            PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
                VALID_NAME,
                VALID_TARGET,
                VALID_CATEGORY,
                user
            );
            PiggyBankAllocation allocation = piggyBank.allocate(
                new Money(BigDecimal.valueOf(100)),
                LocalDate.now(), 
                user
            );

            // Assert
            assertTrue(piggyBank.getAllocations().contains(allocation));
            assertEquals(1, piggyBank.getAllocations().size());
        }
    }

    @Test
    void getCurrentAmountShouldReturnZeroForNewPiggyBank() {
        // Arrange
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            VALID_NAME,
            VALID_TARGET,
            VALID_CATEGORY,
            user
        );
        
        // Act
        Money currentAmount = piggyBank.getCurrentAmount();
        
        // Assert
        assertEquals(new Money(BigDecimal.ZERO), currentAmount);
    }
    
    @Test
    void getCurrentAmountShouldSumAllAllocations() {
        // Arrange
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            VALID_NAME,
            VALID_TARGET,
            VALID_CATEGORY,
            user
        );
        
        // Act
        piggyBank.allocate(new Money(BigDecimal.valueOf(50)), LocalDate.now(), user);
        piggyBank.allocate(new Money(BigDecimal.valueOf(25.50)), LocalDate.now(), user);
        piggyBank.allocate(new Money(BigDecimal.valueOf(124.50)), LocalDate.now(), user);
        Money currentAmount = piggyBank.getCurrentAmount();
        
        // Assert
        assertEquals(new Money(BigDecimal.valueOf(200.00).setScale(2)), currentAmount);
    }
    
    @Test
    void getCurrentAmountShouldWorkForGroupPiggyBank() {
        // Arrange
        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            VALID_NAME,
            VALID_TARGET,
            VALID_CATEGORY,
            group,
            group.getAdmin()
        );
        User member = User.create(
            "member",
            "member@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        group.addMember(member);
        
        // Act - both admin and member add allocations
        piggyBank.allocate(new Money(BigDecimal.valueOf(100)), LocalDate.now(), user);
        piggyBank.allocate(new Money(BigDecimal.valueOf(75)), LocalDate.now(), member);
        Money currentAmount = piggyBank.getCurrentAmount();
        
        // Assert
        assertEquals(new Money(BigDecimal.valueOf(175)), currentAmount);
    }
    
    @Test
    void allocateShouldThrowExceptionForUnauthorizedUser() {
        // Arrange
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            VALID_NAME,
            VALID_TARGET,
            VALID_CATEGORY,
            user
        );
        User unauthorizedUser = User.create(
            "unauthorized",
            "unauthorized@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        
        // Act & Assert
        assertThrows(
            ForbiddenOperationDomainException.class,
            () -> piggyBank.allocate(
                new Money(BigDecimal.valueOf(50)),
                LocalDate.now(),
                unauthorizedUser
            )
        );
        
        // The current amount should still be zero
        assertEquals(new Money(BigDecimal.ZERO), piggyBank.getCurrentAmount());
    }
}
