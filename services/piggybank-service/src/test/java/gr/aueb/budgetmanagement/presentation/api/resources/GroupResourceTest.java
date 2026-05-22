package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class GroupResourceTest extends IntegrationBase {
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String TEST_GROUP_NAME = "Test Group";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture

    @Test
    void testSuccessfulGroupCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        CreateGroupRequest request = new CreateGroupRequest(TEST_GROUP_NAME);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo(TEST_GROUP_NAME))
            .body("is_admin", equalTo(true))
            .header("Location", containsString("/api/v1/groups/"));
    }

    @Test
    void testGroupCreationWithBlankName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        CreateGroupRequest request = new CreateGroupRequest("");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Group name cannot be blank"));
    }

    @Test
    void testGroupCreationWithDuplicateName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        CreateGroupRequest request = new CreateGroupRequest(TEST_GROUP_NAME);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(409)
            .body("message", containsString("Group with name '" + TEST_GROUP_NAME + "' already exists"));
    }

    @Test
    void testGroupCreationWithoutAuthentication() {
        CreateGroupRequest request = new CreateGroupRequest(TEST_GROUP_NAME);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGroupCreationWithInvalidToken() {
        CreateGroupRequest request = new CreateGroupRequest(TEST_GROUP_NAME);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .body(request)
            .when()
            .post(GROUPS_ENDPOINT)
            .then()
            .statusCode(401);
    }

    @Test
    void testGetUserGroups() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("groups", notNullValue())
            .body("groups.size()", is(1))
            .body("groups[0].id", equalTo(1))
            .body("groups[0].name", equalTo("testgroup"))
            .body("groups[0].is_admin", equalTo(true));
    }

    @Test
    void testGetGroupsForUserWithNoGroups() {
        // Create a new user for this test who doesn't belong to any groups
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "nogroups", 
            "nogroups@example.com", 
            "Test123!@#"
        );
        
        String authToken = getAuthTokenRegister(registerRequest);
        
        // Get groups for the user (should be empty)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("groups", notNullValue())
            .body("groups.size()", is(0));
    }

    @Test
    void testGetGroupsWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetGroupsWithInvalidToken() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(401);
    }

    @Test
    void testGetGroupsForNonAdminMember() {
        // Login as testuser3, who is a member but not an admin of testgroup
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            "test3@example.com",
            "Test123!@#"
        );
        
        String authToken = getAuthTokenAuthenticate(loginRequest);
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(GROUPS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("groups", notNullValue())
            .body("groups.size()", is(1))
            .body("groups[0].id", equalTo(1))
            .body("groups[0].name", equalTo("testgroup"))
            .body("groups[0].is_admin", equalTo(false));
    }

    private String getAuthTokenRegister(RegisterUserRequest registerRequest) {
        return given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getString("access_token");
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
