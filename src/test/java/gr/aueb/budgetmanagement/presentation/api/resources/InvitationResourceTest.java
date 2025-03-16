package gr.aueb.budgetmanagement.presentation.api.resources;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.SendInvitationRequest;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@QuarkusTest
class InvitationResourceTest extends IntegrationBase {
    private static final String INVITATIONS_ENDPOINT = "/api/v1/invitations";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
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
            .body("groupId", equalTo(TEST_GROUP_ID.intValue()))
            .body("inviteeId", equalTo(4)) // User ID for test4@example.com
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
            .body("invitations[0].groupId", equalTo(TEST_GROUP_ID.intValue()))
            .body("invitations[0].inviteeId", equalTo(2))
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
            .statusCode(400)
            .body("message", containsString("Invalid status"));
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