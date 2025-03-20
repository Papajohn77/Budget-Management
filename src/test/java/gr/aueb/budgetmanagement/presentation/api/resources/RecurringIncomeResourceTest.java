package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddRecurringIncomeRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.StopRecurringIncomeRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class RecurringIncomeResourceTest extends IntegrationBase {
    private static final String RECURRING_INCOMES_ENDPOINT = "/api/v1/recurring-incomes";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture
    private static final String TEST_INCOME_NAME = "Monthly Salary";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(3000.00);
    private static final IncomeCategory TEST_CATEGORY = IncomeCategory.SALARY;
    private static final LocalDate TEST_START_DATE = LocalDate.now();
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusYears(1);
    private static final boolean TEST_IS_STOPPED = false;

    @Test
    void testSuccessfulRecurringIncomeCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_INCOME_NAME))
                .body("amount", equalTo(3000.00F))
                .body("category", equalTo(TEST_CATEGORY.name()))
                .body("start_date", notNullValue())
                .body("end_date", notNullValue())
                .body("is_stopped", equalTo(TEST_IS_STOPPED));
    }

    @Test
    void testRecurringIncomeCreationWithStopped() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                true // is_stopped = true
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .body("is_stopped", equalTo(true));
    }

    @Test
    void testRecurringIncomeCreationWithNullName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null name
        String requestJson = """
            {
                "name": null,
                "amount": 3000.00,
                "category": "SALARY",
                "start_date": "%s",
                "end_date": "%s",
                "is_stopped": false
            }
            """.formatted(TEST_START_DATE, TEST_END_DATE);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringIncomeCreationWithNullAmount() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null amount
        String requestJson = """
            {
                "name": "Monthly Salary",
                "amount": null,
                "category": "SALARY",
                "start_date": "%s",
                "end_date": "%s",
                "is_stopped": false
            }
            """.formatted(TEST_START_DATE, TEST_END_DATE);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringIncomeCreationWithoutAuthentication() {
        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetUserRecurringIncomes() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest incomeRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(incomeRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_incomes", notNullValue())
                .body("recurring_incomes.size()", greaterThan(0));
    }

    @Test
    void testStopRecurringIncome() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        // Create a recurring income
        Long incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Stop the recurring income
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(204);

        // Verify the income is now stopped
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_incomes.find { it.id == " + incomeId + " }.is_stopped", is(true));
    }

    @Test
    void testStopRecurringIncomeWithIsStopped_False() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        // Create a recurring income
        Long incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        StopRecurringIncomeRequest invalidRequest = new StopRecurringIncomeRequest(false);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(400)
                .body("error", containsString("Only stopping a recurring income is allowed"));
    }

    @Test
    void testStopRecurringIncomeOfAnotherUser() {
        // Login as the first user and create an income
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Register a second user
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "otheruser",
                "otheruser@example.com",
                "Test123!@#"
        );

        String secondUserToken = getAuthTokenRegister(registerRequest);

        // Second user tries to stop first user's recurring income
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteRecurringIncome() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Delete the recurring income
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(204);

        // Verify deletion by checking that the recurring income doesn't appear in the list
        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().asString();

        assertFalse(response.contains("\"id\":" + incomeId + ","),
                "Response still contains the deleted recurring income ID");
    }

    @Test
    void testDeleteRecurringIncomeOfAnotherUser() {
        // Login as the first user and create a recurring income
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Register a second user
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "otheruser2",
                "otheruser2@example.com",
                "Test123!@#"
        );

        String secondUserToken = getAuthTokenRegister(registerRequest);

        // Second user tries to delete first user's recurring income
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(404);
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