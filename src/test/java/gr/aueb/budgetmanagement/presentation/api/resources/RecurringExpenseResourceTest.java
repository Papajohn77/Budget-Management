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

import gr.aueb.budgetmanagement.presentation.api.requests.*;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
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
                TEST_CATEGORY
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
    void testRecurringExpenseCreationWithNullName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
                TEST_CATEGORY
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
                TEST_CATEGORY
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
                TEST_CATEGORY
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

        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);

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
    void testStopRecurringExpenseOfAnotherUser() {
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
                TEST_CATEGORY
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

        AuthenticateUserRequest loginRequest2 = new AuthenticateUserRequest(
                "test2@example.com", // Another user from test fixture
                EXISTING_PASSWORD
        );
        String authToken2 = getAuthTokenAuthenticate(loginRequest2);

        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken2)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(403);
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
                TEST_CATEGORY
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

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);

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
                TEST_CATEGORY
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

        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "otheruser2",
                "otheruser2@example.com",
                "Test123!@#"
        );

        String secondUserToken = getAuthTokenRegister(registerRequest);

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

    @Test
    void testInvalidJWTForGetRecurringExpenses() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForCreateRecurringExpense() {
        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForUpdateRecurringExpense() {
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForDeleteRecurringExpense() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testUpdateNonExistentRecurringExpense() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        Long nonExistentId = 99999L;
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteNonExistentRecurringExpense() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        Long nonExistentId = 99999L;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testRecurringExpenseCreationWithNegativeAmount() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                BigDecimal.valueOf(-100.00),
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringExpenseCreationWithEndDateBeforeStartDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_START_DATE.minusDays(1), // End date before start date
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testMalformedAuthorizationHeaderForGet() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123")  // Missing Bearer prefix
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForGet() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testMissingRequiredParametersInUpdateRequest() {
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
                TEST_CATEGORY
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

        String malformedUpdateJson = "{}"; // Missing is_stopped field

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(malformedUpdateJson)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringExpenseCreationWithEmptyName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                "",  // Empty string instead of null
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringExpenseCreationWithNullCategory() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
        {
            "name": "Monthly Rent",
            "amount": 1200.00,
            "category": null,
            "start_date": "%s",
            "end_date": "%s"
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
    void testInvalidJWTForUpdateFullRecurringExpense() {
        UpdateRecurringExpenseRequest updateRequest = new UpdateRecurringExpenseRequest(
                TEST_START_DATE.plusMonths(1),
                BigDecimal.valueOf(1500.00),
                ExpenseCategory.OTHER
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .body(updateRequest)
                .when()
                .put(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testSuccessfulRecurringExpenseStop() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // First create a recurring expense
        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                "Test Expense",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        // Now stop the recurring expense
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);
    }

    @Test
    void testStopRecurringExpenseWithInvalidValue() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // First create a recurring expense
        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                "Test Expense",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        // Try to stop with invalid value (false instead of true)
        StopRecurringExpenseRequest invalidRequest = new StopRecurringExpenseRequest(false);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(400);
    }

    @Test
    void testStopNonExistentRecurringExpense() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Use a non-existent ID
        Long nonExistentId = 999999L;
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testStopOtherUserRecurringExpense() {
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringExpenseRequest createRequest = new AddRecurringExpenseRequest(
                "First User Expense",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                "test2@example.com",
                EXISTING_PASSWORD
        );

        String secondUserToken = getAuthTokenAuthenticate(loginRequest);

        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(403);
    }

    @Test
    void testStopRecurringExpenseWithoutAuthentication() {
        // Try to stop an expense without authentication
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/1")  // Any ID will do as auth should fail first
                .then()
                .statusCode(401);
    }

    @Test
    void testStopRecurringExpenseWithInvalidToken() {
        // Try to stop an expense with invalid token
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidToken123")
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/1")  // Any ID will do as auth should fail first
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForCreateRecurringExpense() {
        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForCreateRecurringExpense() {
        AddRecurringExpenseRequest request = new AddRecurringExpenseRequest(
                TEST_EXPENSE_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123") // Missing Bearer prefix
                .body(request)
                .when()
                .post(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForUpdateRecurringExpense() {
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForUpdateRecurringExpense() {
        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123") // Missing Bearer prefix
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForDeleteRecurringExpense() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForDeleteRecurringExpense() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123") // Missing Bearer prefix
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetRecurringExpensesWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testRecurringExpenseCreationWithMissingEndDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
        {
            "name": "Monthly Rent",
            "amount": 1200.00,
            "category": "HOUSING",
            "start_date": "%s"
        }
        """.formatted(TEST_START_DATE);

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
    void testRecurringExpenseCreationWithMissingStartDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
        {
            "name": "Monthly Rent",
            "amount": 1200.00,
            "category": "HOUSING",
            "end_date": "%s"
        }
        """.formatted(TEST_END_DATE);

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
    void testGetEmptyRecurringExpenses() {
        // Create a new user with no recurring expenses
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "newuser",
                "newuser@example.com",
                "Test123!@#"
        );

        String newUserToken = getAuthTokenRegister(registerRequest);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + newUserToken)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_expenses.size()", equalTo(0));
    }


}
