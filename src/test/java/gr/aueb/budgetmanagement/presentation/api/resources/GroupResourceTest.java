package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class GroupResourceTest extends IntegrationBase {
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String TEST_GROUP_NAME = "Test Group";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture

    @Test
    void testSuccessfulGroupCreation() {
        String authToken = getAuthToken();
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
        String authToken = getAuthToken();
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
        String authToken = getAuthToken();

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

    private String getAuthToken() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );

        Response loginResponse = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().response();

        return loginResponse.jsonPath().getString("access_token");
    }
}
