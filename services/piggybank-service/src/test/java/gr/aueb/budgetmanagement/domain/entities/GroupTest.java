package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class GroupTest {
    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.create(1L);
    }

    @Test
    void createWithValidData() {
        Group group = Group.create("Test Group", admin);

        assertEquals("Test Group", group.getName());
        assertEquals(admin, group.getAdmin());
        assertTrue(group.getMembers().contains(admin));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    void createWithNullName() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create(null, admin)
        );
    }

    @Test
    void createWithBlankName() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create("   ", admin)
        );
    }

    @Test
    void createWithNullAdmin() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create("Test Group", null)
        );
    }

    @Test
    void addMemberWithValidUserShouldAddToMembers() {
        Group group = Group.create("Test Group", admin);
        User newMember = createNonAdminUser();

        group.addMember(newMember);

        assertTrue(group.getMembers().contains(newMember));
        assertEquals(2, group.getMembers().size());
    }

    @Test
    void addMemberWithNullUserShouldThrowException() {
        Group group = Group.create("Test Group", admin);

        assertThrows(
            InvalidDomainArgumentException.class,
            () -> group.addMember(null)
        );
    }

    @Test
    void addMemberWhenMemberAlreadyExistsShouldNotDuplicate() {
        Group group = Group.create("Test Group", admin);

        group.addMember(admin);

        assertEquals(1, group.getMembers().size());
    }

    @Test
    void getMembersShouldReturnUnmodifiableSet() {
        Group group = Group.create("Test Group", admin);
        Set<User> members = group.getMembers();

        assertThrows(
            UnsupportedOperationException.class,
            () -> members.add(new User())
        );
    }

    @Test
    void addPiggyBankShouldAddToGroup() {
        Group group = Group.create("Test Group", admin);
        GroupPiggyBank piggyBank = createGroupPiggyBank(group);

        assertTrue(group.getPiggyBanks().contains(piggyBank));
        assertEquals(1, group.getPiggyBanks().size());
    }

    @Test
    void addPiggyBankWithNullPiggyBank() {
        // Arrange
        Group group = Group.create("Test Group", admin);

        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> group.addPiggyBank(null)
        );
    }

    @Test
    void addPiggyBankTwiceShouldNotDuplicate() {
        // Arrange
        Group group = Group.create("Test Group", admin);
        GroupPiggyBank piggyBank = createGroupPiggyBank(group);

        // Act
        group.addPiggyBank(piggyBank); // Adding again

        // Assert
        assertEquals(1, group.getPiggyBanks().size());
    }

    @Test
    void getPiggyBanksShouldReturnUnmodifiableSet() {
        Group group = Group.create("Test Group", admin);
        Set<GroupPiggyBank> piggyBanks = group.getPiggyBanks();

        assertThrows(
            UnsupportedOperationException.class,
            () -> piggyBanks.add(new GroupPiggyBank())
        );
    }

    private User createNonAdminUser() {
        return User.create(2L);
    }

    private GroupPiggyBank createGroupPiggyBank(Group group) {
        return GroupPiggyBank.create(
            "Test Piggy Bank",
            new Money(new BigDecimal("100.00")),
            ExpenseCategory.OTHER,
            group, 
            group.getAdmin()
        );
    }
}
