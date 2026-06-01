package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.application.clients.IdentityClient;
import gr.aueb.budgetmanagement.application.clients.UserIdRepresentation;
import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.presentation.api.requests.SendInvitationRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateInvitationStatusRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@QuarkusTest
class InvitationResourceTest extends IntegrationBase {
    private static final String INVITATIONS_ENDPOINT = "/api/v1/invitations";
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final Long TEST_GROUP_ID = 1L;
    private static final String NON_EXISTENT_EMAIL = "nonexistent@example.com";

    @InjectMock
    @RestClient
    IdentityClient identityClient;

    @Test
    void testSuccessfulInvitationCreation() {
        when(identityClient.findByEmail("test4@example.com"))
            .thenReturn(new UserIdRepresentation(Fixture.Users.TESTUSER4_ID));

        SendInvitationRequest request = new SendInvitationRequest(TEST_GROUP_ID, "test4@example.com");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authTokenForTestUser())
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("group_id", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitee_id", equalTo(Fixture.Users.TESTUSER4_ID.intValue()))
            .body("status", equalTo("PENDING"));
    }

    @Test
    void testInvitationCreationWithNonExistentEmail() {
        when(identityClient.findByEmail(NON_EXISTENT_EMAIL))
            .thenThrow(new WebApplicationException(Response.status(404).build()));

        SendInvitationRequest request = new SendInvitationRequest(TEST_GROUP_ID, NON_EXISTENT_EMAIL);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authTokenForTestUser())
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(404)
            .body("message", containsString("Invitee not found with email"));
    }

    @Test
    void testInvitationCreationWithNonExistentGroup() {
        SendInvitationRequest request = new SendInvitationRequest(999L, "test4@example.com");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authTokenForTestUser())
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(404)
            .body("message", containsString("Group not found with id"));
    }

    @Test
    void testInvitationCreationByNonAdmin() {
        when(identityClient.findByEmail(anyString()))
            .thenReturn(new UserIdRepresentation(Fixture.Users.TESTUSER4_ID));

        // User 3 is a member of group 1 but not the admin
        SendInvitationRequest request = new SendInvitationRequest(TEST_GROUP_ID, "test4@example.com");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authTokenFor(Fixture.Users.TESTUSER3_ID))
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(403)
            .body("message", containsString("Only the group admin can send invitations"));
    }

    @Test
    void testInvitationCreationWithoutAuthentication() {
        SendInvitationRequest request = new SendInvitationRequest(TEST_GROUP_ID, "test4@example.com");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDuplicateInvitation() {
        // User 2 already has a pending invitation to group 1 from the test fixture
        when(identityClient.findByEmail("test2@example.com"))
            .thenReturn(new UserIdRepresentation(Fixture.Users.TESTUSER2_ID));

        SendInvitationRequest request = new SendInvitationRequest(TEST_GROUP_ID, "test2@example.com");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authTokenForTestUser())
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(409)
            .body("message", containsString("Invitation already exists"));
    }

    @Test
    void testGetPendingInvitations() {
        // testuser2 (id=2) already has a pending invitation in the fixture
        String inviteeAuthToken = authTokenForSecondTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .when()
            .get(INVITATIONS_ENDPOINT + "?status=PENDING")
            .then()
            .statusCode(200)
            .body("invitations", notNullValue())
            .body("invitations.size()", is(1))
            .body("invitations[0].group_id", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitations[0].invitee_id", equalTo(2))
            .body("invitations[0].status", equalTo("PENDING"));
    }

    @Test
    void testGetAllInvitations() {
        String inviteeAuthToken = authTokenForSecondTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .when()
            .get(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("invitations", notNullValue())
            .body("invitations.size()", is(1));
    }

    @Test
    void testGetInvitationsWithInvalidStatus() {
        String inviteeAuthToken = authTokenForSecondTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .when()
            .get(INVITATIONS_ENDPOINT + "?status=INVALID_STATUS")
            .then()
            .statusCode(500);
    }

    @Test
    void testGetInvitationsWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testAcceptInvitation() {
        // testuser2 (id=2) has a pending invitation in the fixture
        String inviteeAuthToken = authTokenForSecondTestUser();

        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.ACCEPT);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(200)
            .body("group_id", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitee_id", equalTo(Fixture.Users.TESTUSER2_ID.intValue()))
            .body("status", equalTo("ACCEPTED"));

        // Verify the user is now a member of the group
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("groups.size()", is(1))
            .body("groups[0].id", equalTo(TEST_GROUP_ID.intValue()))
            .body("groups[0].is_admin", equalTo(false));
    }

    @Test
    void testRejectInvitation() {
        String inviteeAuthToken = authTokenForSecondTestUser();

        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.REJECT);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(200)
            .body("group_id", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitee_id", equalTo(Fixture.Users.TESTUSER2_ID.intValue()))
            .body("status", equalTo("REJECTED"));
    }

    @Test
    void testUpdateInvitationWithoutAuthentication() {
        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(
            InvitationResponseOperationType.ACCEPT
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testUpdateInvitationWithInvalidToken() {
        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(
            InvitationResponseOperationType.ACCEPT
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(401);
    }

    @Test
    void testUpdateInvitationNonExistentInvitation() {
        String inviteeAuthToken = authTokenForSecondTestUser();

        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.ACCEPT);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/999/")
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateAlreadyAcceptedInvitation() {
        String inviteeAuthToken = authTokenForSecondTestUser();

        UpdateInvitationStatusRequest acceptRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.ACCEPT);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(acceptRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(acceptRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(409)
            .body("message", containsString("Can only accept pending invitations"));
    }
}
