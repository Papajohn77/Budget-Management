package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusTest
class UserResourceTest extends IntegrationBase {
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String TEST_USERNAME = "testuser123";
    private static final String TEST_EMAIL = "testuser123@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_USERNAME = "testuser"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture

    @Test
    void testSuccessfulRegistration() throws JsonProcessingException {
        RegisterUserRequest request = new RegisterUserRequest(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(201)
            .body("token_type", equalTo("Bearer"))
            .body("access_token", notNullValue())
            .extract().response();

        // Verify token structure
        String accessToken = response.jsonPath().getString("access_token");
        assertValidAccessToken(accessToken);
    }

    @Test
    void testRegistrationWithExistingUsername() {
        RegisterUserRequest request = new RegisterUserRequest(
            EXISTING_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(409)
            .body("message", containsString("Username already exists"));
    }

    @Test
    void testRegistrationWithExistingEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
            TEST_USERNAME,
            EXISTING_EMAIL,
            TEST_PASSWORD
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(409)
            .body("message", containsString("Email already exists"));
    }

    @Test
    void testRegistrationWithInvalidEmail() {
        RegisterUserRequest request = new RegisterUserRequest(
            TEST_USERNAME,
            "invalid-email",
            TEST_PASSWORD
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Invalid email"));
    }

    @Test
    void testRegistrationWithWeakPassword() {
        RegisterUserRequest request = new RegisterUserRequest(
            TEST_USERNAME,
            TEST_EMAIL,
            "weak"
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(REGISTER_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Password must"));
    }

    @Test
    void testSuccessfulAuthentication() throws JsonProcessingException {
        AuthenticateUserRequest request = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(200)
            .body("token_type", equalTo("Bearer"))
            .body("access_token", notNullValue())
            .extract().response();

        // Verify token structure
        String accessToken = response.jsonPath().getString("access_token");
        assertValidAccessToken(accessToken);
    }

    @Test
    void testAuthenticationWithNonexistentEmail() {
        AuthenticateUserRequest request = new AuthenticateUserRequest(
            "nonexistent@example.com",
            EXISTING_PASSWORD
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Invalid credentials"));
    }

    @Test
    void testAuthenticationWithWrongPassword() {
        AuthenticateUserRequest request = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            "WrongPassword123!@#"
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Invalid credentials"));
    }

    @Test
    void testAuthenticationWithEmptyEmail() {
        AuthenticateUserRequest request = new AuthenticateUserRequest(
            "",
            EXISTING_PASSWORD
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(400);
    }

    @Test
    void testAuthenticationWithEmptyPassword() {
        AuthenticateUserRequest request = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            ""
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(LOGIN_ENDPOINT)
            .then()
            .statusCode(400);
    }

    private void assertValidAccessToken(String accessToken) throws JsonProcessingException {
        String[] parts = accessToken.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");

        // Decode and verify essential claims
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode claims = mapper.readTree(payload);

        // Verify essential user claims
        assertTrue(claims.has("sub"));
        assertTrue(claims.has("user_id"));
        assertNotNull(claims.get("exp"));
    }
}
