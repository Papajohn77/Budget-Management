package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateExpenseRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ExpenseResourceTest extends IntegrationBase {
    private static final String EXPENSES_ENDPOINT = "/api/v1/expenses";
    private static final String EXPENSE_CATEGORIES_ENDPOINT = "/api/v1/expenses/categories";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(53.75);
    private static final BigDecimal TEST_UPDATE_AMOUNT = BigDecimal.valueOf(150);
    private static final LocalDate TEST_DATE = LocalDate.now();
    private static final ExpenseCategory TEST_CATEGORY = ExpenseCategory.HOUSING;
    private static final ExpenseCategory TEST_CATEGORY_UPDATE = ExpenseCategory.ENTERTAINMENT; ;

    @Test
    void testGetExpenseCategories() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );

        String authToken = getAuthTokenAuthenticate(loginRequest);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(EXPENSE_CATEGORIES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("expense_categories", containsInAnyOrder(
                        Arrays.stream(ExpenseCategory.values()).map(Enum::name).toArray(String[]::new)
                ));

    }

    @Test
    void testSuccessfulExpenseCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddExpenseRequest request = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(request)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("amount", equalTo(53.75F))
                .body("category", equalTo(TEST_CATEGORY.name()))
                .body("date", notNullValue());
    }

    @Test
    void testExpenseCreationWithNullAmount() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null amount
        String requestJson = """
            {
                "amount": null,
                "category": "FOOD",
                "date": "%s"
            }
            """.formatted(LocalDate.now());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testExpenseCreationWithNullCategory() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null category
        String requestJson = """
            {
                "amount": 25.50,
                "category": null,
                "date": "%s"
            }
            """.formatted(LocalDate.now());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testExpenseCreationWithNullDate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with JSON that has null date
        String requestJson = """
            {
                "amount": 25.50,
                "category": "FOOD",
                "date": null
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }


    @Test
    void testExpenseCreationWithInvalidCategory() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create request with invalid category
        String requestJson = """
            {
                "amount": 25.50,
                "category": "INVALID_CATEGORY",
                "date": "%s"
            }
            """.formatted(LocalDate.now());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(requestJson)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    void testExpenseCreationWithoutAuthentication() {
        AddExpenseRequest request = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetUserExpenses() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(expenseRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201);

        String responseBody = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().body().asString();

        System.out.println("Response body: " + responseBody);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("expenses", notNullValue())
                .body("expenses[0].amount", is(53.75F));
    }

    @Test
    void testGetExpensesWithDateFilters() {
        // Setup
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        LocalDate fromDate = LocalDate.now().minusMonths(1);
        LocalDate toDate = LocalDate.now();

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
                LocalDate.now().minusDays(15), // Date between fromDate and toDate
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(expenseRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201);

        // Test the filter
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("from_date", fromDate.toString())
                .queryParam("to_date", toDate.toString())
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("expenses", notNullValue())
                .body("expenses.size()", greaterThan(0));

    }

    @Test
    void testGetExpensesWithCategoryFilter() {

        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );

        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(expenseRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("category", TEST_CATEGORY.name())
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .body("expenses", notNullValue())
                .body("expenses.size()", greaterThan(0))
                .body("expenses.findAll { it.category == '" + TEST_CATEGORY.name() + "' }.size()", greaterThan(0));
    }

    @Test
    void testUpdateExpenseWithInvalidData() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddExpenseRequest createRequest = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        // Extract the ID of the created expense
        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Try to update with null values
        String invalidUpdateJson = """
            {
                "amount": null,
                "date": null,
                "category": null
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(invalidUpdateJson)
                .when()
                .put(EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(500);
    }

    @Test
    void testUpdateExpenseOfAnotherUser() {
        // Login as the first user and create an expense
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddExpenseRequest createRequest = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + firstUserToken)
                .body(createRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
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

        // Second user tries to update first user's expense
        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY_UPDATE
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .body(updateRequest)
                .when()
                .put(EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteExpense() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddExpenseRequest createRequest = new AddExpenseRequest(
                LocalDate.now(),
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        Long expenseId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(createRequest)
                .when()
                .post(EXPENSES_ENDPOINT)
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Delete the expense
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(204);

        //Verify deletion by checking that the expense doesn't appear in the list of all expenses
        String response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().asString();

        assertFalse(response.contains("\"id\":" + expenseId + ","),
                "Response still contains the deleted expense ID");
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
