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
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddRecurringExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.StopRecurringExpenseRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class RecurringExpenseResourceTest extends IntegrationBase {
    private static final String RECURRING_EXPENSES_ENDPOINT = "/api/v1/recurring-expenses";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture
    private static final String TEST_EXPENSE_NAME = "Monthly Rent";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1200.00);
    private static final ExpenseCategory TEST_CATEGORY = ExpenseCategory.HOUSING;
    private static final LocalDate TEST_START_DATE = LocalDate.now();
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusYears(1);
    private static final boolean TEST_IS_STOPPED = false;

    @Test
    void testSuccessfulRecurringExpenseCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
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
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(TEST_EXPENSE_NAME))
                .body("amount", equalTo(1200.00F))
                .body("category", equalTo(TEST_CATEGORY.name()))
                .body("start_date", notNullValue())
                .body("end_date", notNullValue())
                .body("is_stopped", equalTo(TEST_IS_STOPPED));
    }

    @Test
    void testRecurringExpenseCreationWithStopped() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
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
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .body("is_stopped", equalTo(true));
    }

    @Test
    void testRecurringExpenseCreationWithNullName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null name
        String requestJson = """
            {
                "name": null,
                "amount": 1200.00,
                "category": "HOUSING",
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
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringExpenseCreationWithNullAmount() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null amount
        String requestJson = """
            {
                "name": "Monthly Rent",
                "amount": null,
                "category": "HOUSING",
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
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringExpenseCreationWithoutAuthentication() {
        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
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
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetUserRecurringExpenses() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest expenseRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(expenseRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_expenses", notNullValue())
                .body("recurring_expenses.size()", greaterThan(0));
    }

    @Test
    void testStopRecurringExpense() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        // Create a recurring expense
        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Stop the recurring expense
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);

        // Verify the expense is now stopped
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_expenses.find { it.id == " + expenseId + " }.is_stopped", is(true));
    }

    @Test
    void testStopRecurringExpenseWithIsStopped_False() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        // Create a recurring expense
        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        StopRecurringExpenseRequest invalidRequest = new StopRecurringExpenseRequest(false);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(400)
                .body("error", containsString("Only stopping a recurring expense is allowed"));
    }

    @Test
    void testStopRecurringExpenseOfAnotherUser() {
        // Login as the first user and create an expense
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
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

        // Second user tries to stop first user's recurring expense
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteRecurringExpense() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Delete the recurring expense
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);

        // Verify deletion by checking that the recurring expense doesn't appear in the list
        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().asString();

        assertFalse(response.contains("\"id\":" + expenseId + ","),
                "Response still contains the deleted recurring expense ID");
    }

    @Test
    void testDeleteRecurringExpenseOfAnotherUser() {
        // Login as the first user and create a recurring expense
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY,
                TEST_IS_STOPPED
        );

        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
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

        // Second user tries to delete first user's recurring expense
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
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