package gr.aueb.budgetmanagement.presentation.api.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.SendInvitationRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateInvitationStatusRequest;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@QuarkusTest
class InvitationResourceTest extends IntegrationBase {
    private static final String INVITATIONS_ENDPOINT = "/api/v1/invitations";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final Long TEST_GROUP_ID = 1L; // From test fixture
    private static final String ADMIN_EMAIL = "test@example.com"; // From test fixture
    private static final String ADMIN_PASSWORD = "Test123!@#"; // From test fixture
    private static final String INVITEE_EMAIL = "test2@example.com"; // From test fixture
    private static final String NON_EXISTENT_EMAIL = "nonexistent@example.com";

    @Test
    void testSuccessfulInvitationCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            ADMIN_EMAIL,
            ADMIN_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Use test4@example.com instead of test2@example.com
        String differentInviteeEmail = "test4@example.com";

        SendInvitationRequest request = new SendInvitationRequest(
            TEST_GROUP_ID,
            differentInviteeEmail
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("group_id", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitee_id", equalTo(4)) // User ID for test4@example.com
            .body("status", equalTo("PENDING"));
    }

    @Test
    void testInvitationCreationWithNonExistentEmail() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            ADMIN_EMAIL,
            ADMIN_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        SendInvitationRequest request = new SendInvitationRequest(
            TEST_GROUP_ID,
            NON_EXISTENT_EMAIL
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(404)
            .body("message", containsString("Invitee not found with email"));
    }

    @Test
    void testInvitationCreationWithNonExistentGroup() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            ADMIN_EMAIL,
            ADMIN_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        SendInvitationRequest request = new SendInvitationRequest(
            999L, // Non-existent group ID
            INVITEE_EMAIL
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(404)
            .body("message", containsString("Group not found with id"));
    }

    @Test
    void testInvitationCreationByNonAdmin() {
        // Login as a non-admin user (test3@example.com from fixture)
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            "test3@example.com",
            "Test123!@#"
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        SendInvitationRequest request = new SendInvitationRequest(
            TEST_GROUP_ID,
            INVITEE_EMAIL
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(403)
            .body("message", containsString("Only the group admin can send invitations"));
    }

    @Test
    void testInvitationCreationWithoutAuthentication() {
        SendInvitationRequest request = new SendInvitationRequest(
            TEST_GROUP_ID,
            INVITEE_EMAIL
        );

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            ADMIN_EMAIL,
            ADMIN_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String differentInviteeEmail = "test4@example.com";

        SendInvitationRequest request = new SendInvitationRequest(
            TEST_GROUP_ID,
            differentInviteeEmail
        );

        // First invitation should succeed
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(201);

        // Second invitation with same details should fail
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(409)
            .body("message", containsString("Invitation already exists"));
    }

    @Test
    void testGetPendingInvitations() {
        // Login as invitee to check pending invitations
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com", // This user already has a pending invitation
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Get pending invitations for invitee
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
        // Login as invitee to check all invitations
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com",
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Get all invitations for invitee
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .when()
            .get(INVITATIONS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("invitations", notNullValue())
            .body("invitations.size()", is(1)); // User test2 only has one invitation in the test data
    }

    @Test
    void testGetInvitationsWithInvalidStatus() {
        // Login as invitee
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com",
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Get invitations with invalid status
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
        // Login as invitee to accept invitation
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com", // This user already has a pending invitation
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Accept invitation
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
        // Login as invitee to reject invitation
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com", // This user already has a pending invitation
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Reject invitation
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
    void testUpdateInvitationNonExistentInvitation() {
        // Login as invitee
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com",
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // Update non-existent invitation
        UpdateInvitationStatusRequest updateRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.ACCEPT);
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(updateRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/999/") // Non-existent group ID
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateAlreadyAcceptedInvitation() {
        // Login as invitee
        AuthenticateUserRequest inviteeLoginRequest = new AuthenticateUserRequest(
            "test2@example.com",
            "Test123!@#"
        );
        String inviteeAuthToken = getAuthTokenAuthenticate(inviteeLoginRequest);

        // First accept the invitation
        UpdateInvitationStatusRequest acceptRequest = new UpdateInvitationStatusRequest(InvitationResponseOperationType.ACCEPT);
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + inviteeAuthToken)
            .body(acceptRequest)
            .when()
            .patch(INVITATIONS_ENDPOINT + "/" + TEST_GROUP_ID)
            .then()
            .statusCode(200);
        
        // Try to update again
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

    private String getAuthTokenAuthenticate(AuthenticateUserRequest loginRequest) {
        return given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().jsonPath().getString("access_token");
    }
}