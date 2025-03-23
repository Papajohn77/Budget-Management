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
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
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
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(3500.00);
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
                TEST_CATEGORY
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
                .body("amount", equalTo(3500.00F))
                .body("category", equalTo(TEST_CATEGORY.name()))
                .body("start_date", notNullValue())
                .body("end_date", notNullValue())
                .body("is_stopped", equalTo(TEST_IS_STOPPED));
    }

    @Test
    void testRecurringIncomeCreationWithNullName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
                {
                    "name": null,
                    "amount": 3500.00,
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
                TEST_CATEGORY
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
                TEST_CATEGORY
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
                TEST_CATEGORY
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

        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(204);

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
                TEST_CATEGORY
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

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(204);

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
                TEST_CATEGORY
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

    @Test
    void testInvalidJWTForGetRecurringIncomes() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForCreateRecurringIncome() {
        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForUpdateRecurringIncome() {
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testInvalidJWTForDeleteRecurringIncome() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testUpdateNonExistentRecurringIncome() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        Long nonExistentId = 99999L;
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteNonExistentRecurringIncome() {
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
                .delete(RECURRING_INCOMES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testRecurringIncomeCreationWithNegativeAmount() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringIncomeCreationWithEndDateBeforeStartDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testMalformedAuthorizationHeaderForGet() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123")  // Missing Bearer prefix
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForGet() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
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

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
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

        String malformedUpdateJson = "{}"; // Missing is_stopped field

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(malformedUpdateJson)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringIncomeCreationWithEmptyName() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testRecurringIncomeCreationWithNullCategory() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
                {
                    "name": "Monthly Salary",
                    "amount": 3500.00,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testInvalidJWTForUpdateFullRecurringIncome() {
        UpdateRecurringIncomeRequest updateRequest = new UpdateRecurringIncomeRequest(
                TEST_START_DATE.plusMonths(1),
                BigDecimal.valueOf(4000.00),
                IncomeCategory.OTHER
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidtoken123")
                .body(updateRequest)
                .when()
                .put(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testSuccessfulRecurringIncomeStop() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // First create a recurring income
        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                "Test Income",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        // Now stop the recurring income
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(204);
    }

    @Test
    void testStopRecurringIncomeWithInvalidValue() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // First create a recurring income
        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                "Test Income",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        // Try to stop with invalid value (false instead of true)
        StopRecurringIncomeRequest invalidRequest = new StopRecurringIncomeRequest(false);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(400);
    }

    @Test
    void testStopNonExistentRecurringIncome() {
        // Login and get token
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Use a non-existent ID
        Long nonExistentId = 999999L;
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testStopOtherUserRecurringIncome() {
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddRecurringIncomeRequest createRequest = new AddRecurringIncomeRequest(
                "First User Income",
                TEST_START_DATE,
                TEST_END_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Integer incomeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getInt("id");

        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                "test2@example.com",
                EXISTING_PASSWORD
        );

        String secondUserToken = getAuthTokenAuthenticate(loginRequest);

        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/" + incomeId)
                .then()
                .statusCode(403);
    }

    @Test
    void testStopRecurringIncomeWithoutAuthentication() {
        // Try to stop an income without authentication
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/1")  // Any ID will do as auth should fail first
                .then()
                .statusCode(401);
    }

    @Test
    void testStopRecurringIncomeWithInvalidToken() {
        // Try to stop an income with invalid token
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalidToken123")
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/1")  // Any ID will do as auth should fail first
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForCreateRecurringIncome() {
        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForCreateRecurringIncome() {
        AddRecurringIncomeRequest request = new AddRecurringIncomeRequest(
                TEST_INCOME_NAME,
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
                .post(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForUpdateRecurringIncome() {
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForUpdateRecurringIncome() {
        StopRecurringIncomeRequest stopRequest = new StopRecurringIncomeRequest(true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123") // Missing Bearer prefix
                .body(stopRequest)
                .when()
                .patch(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testEmptyAuthorizationHeaderForDeleteRecurringIncome() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "")
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testMalformedAuthorizationHeaderForDeleteRecurringIncome() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "invalidtoken123") // Missing Bearer prefix
                .when()
                .delete(RECURRING_INCOMES_ENDPOINT + "/1")
                .then()
                .statusCode(401);
    }

    @Test
    void testGetRecurringIncomesWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(401);
    }

    @Test
    void testRecurringIncomeCreationWithMissingEndDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
                {
                    "name": "Monthly Salary",
                    "amount": 1200.00,
                    "category": "JOB",
                    "start_date": "%s"
                }
                """.formatted(TEST_START_DATE);

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
    void testRecurringIncomeCreationWithMissingStartDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
                {
                    "name": "Monthly Salary",
                    "amount": 1200.00,
                    "category": "JOB",
                    "end_date": "%s"
                }
                """.formatted(TEST_END_DATE);

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
    void testGetEmptyRecurringIncomes() {
        // Create a new user with no recurring incomes
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
                .get(RECURRING_INCOMES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_incomes.size()", equalTo(0));
    }
}
