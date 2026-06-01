package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class GroupResourceTest extends IntegrationBase {
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final String TEST_GROUP_NAME = "Test Group";

    @Test
    void testSuccessfulGroupCreation() {
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();
        
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
        String authToken = authTokenFor(Fixture.Users.TESTUSER4_ID);
        
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
        String authToken = authTokenFor(Fixture.Users.TESTUSER3_ID);
        
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

}
