package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddRecurringExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.StopRecurringExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateRecurringExpenseRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class RecurringExpenseResourceTest extends IntegrationBase {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final String RECURRING_EXPENSES_ENDPOINT = "/api/v1/recurring-expenses";
    private static final String TEST_EXPENSE_NAME = "Monthly Rent";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1200.00);
    private static final ExpenseCategory TEST_CATEGORY = ExpenseCategory.HOUSING;
    private static final LocalDate TEST_START_DATE = FIXED_DATE;
    private static final LocalDate TEST_END_DATE = FIXED_DATE.plusYears(1);
    private static final boolean TEST_IS_STOPPED = false;

    @Test
    void testSuccessfulRecurringExpenseCreation() {
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
    void testStopRecurringExpenseOfAnotherUser() throws JsonProcessingException {
        String firstUserToken = authTokenForTestUser();

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

        String authToken2 = authTokenForSecondTestUser();

        StopRecurringExpenseRequest stopRequest = new StopRecurringExpenseRequest(true);

        String message = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken2)
                .body(stopRequest)
                .when()
                .patch(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(404)
                .extract().jsonPath().getString("message");

        String expectedMessage = "Not Found: Recurring expense with id: " + expenseId 
                + " was not found for user with id: " + getUserIdClaim(authToken2);
        assertEquals(expectedMessage, message);
    }

    @Test
    void testDeleteRecurringExpense() {
        String authToken = authTokenForTestUser();

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
        String firstUserToken = authTokenForTestUser();

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

        String secondUserToken = authTokenForSecondTestUser();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .when()
                .delete(RECURRING_EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(404);
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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

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
        String secondUserToken = authTokenForSecondTestUser();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .when()
                .get(RECURRING_EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("recurring_expenses.size()", equalTo(0));
    }

    private String getUserIdClaim(String accessToken) throws JsonProcessingException {
        String[] parts = accessToken.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode claims = mapper.readTree(payload);
        return claims.get("user_id").asText();
    }
}
