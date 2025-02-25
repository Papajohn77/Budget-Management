package gr.aueb.budgetmanagement.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

class InvitationIdTest {
    private static final Long GROUP_ID = 1L;
    private static final Long INVITEE_ID = 2L;

    @Test
    void createInvitationId_WithValidIds_ShouldSucceed() {
        // Act
        InvitationId invitationId = new InvitationId(GROUP_ID, INVITEE_ID);

        // Assert
        assertNotNull(invitationId);
    }

    @Test
    void equals_WithSameValues_ShouldBeEqual() {
        // Arrange
        InvitationId invitationId1 = new InvitationId(GROUP_ID, INVITEE_ID);
        InvitationId invitationId2 = new InvitationId(GROUP_ID, INVITEE_ID);

        // Assert
        assertEquals(invitationId1, invitationId2);
        assertEquals(invitationId1.hashCode(), invitationId2.hashCode());
    }

    @Test
    void equals_WithDifferentValues_ShouldNotBeEqual() {
        // Arrange
        InvitationId invitationId1 = new InvitationId(GROUP_ID, INVITEE_ID);
        InvitationId invitationId2 = new InvitationId(GROUP_ID, 3L);
        InvitationId invitationId3 = new InvitationId(4L, INVITEE_ID);

        // Assert
        assertNotEquals(invitationId1, invitationId2);
        assertNotEquals(invitationId1, invitationId3);
        assertNotEquals(invitationId2, invitationId3);
    }

    @Test
    void equals_WithSameInstance_ShouldBeEqual() {
        // Arrange
        InvitationId invitationId = new InvitationId(GROUP_ID, INVITEE_ID);

        // Assert
        assertEquals(invitationId, invitationId);
    }

    @Test
    void equals_WithDifferentType_ShouldNotBeEqual() {
        // Arrange
        InvitationId invitationId = new InvitationId(GROUP_ID, INVITEE_ID);
        Object other = new Object();

        // Assert
        assertNotEquals(invitationId, other);
    }

    @ParameterizedTest
    @NullSource
    void equals_WithNull_ShouldNotBeEqual(Object other) {
        // Arrange
        InvitationId invitationId = new InvitationId(GROUP_ID, INVITEE_ID);

        // Assert
        assertNotEquals(invitationId, other);
    }

    @Test
    void hashCode_WithSameValues_ShouldBeEqual() {
        // Arrange
        InvitationId invitationId1 = new InvitationId(GROUP_ID, INVITEE_ID);
        InvitationId invitationId2 = new InvitationId(GROUP_ID, INVITEE_ID);

        // Assert
        assertEquals(invitationId1.hashCode(), invitationId2.hashCode());
    }

    @Test
    void hashCode_WithDifferentValues_ShouldNotBeEqual() {
        // Arrange
        InvitationId invitationId1 = new InvitationId(GROUP_ID, INVITEE_ID);
        InvitationId invitationId2 = new InvitationId(GROUP_ID, 3L);

        // Assert
        assertNotEquals(invitationId1.hashCode(), invitationId2.hashCode());
    }
}