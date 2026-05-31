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

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddExpenseRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateExpenseRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ExpenseResourceTest extends IntegrationBase {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final String EXPENSES_ENDPOINT = "/api/v1/expenses";
    private static final String EXPENSE_CATEGORIES_ENDPOINT = "/api/v1/expenses/categories";    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(53.75);
    private static final BigDecimal TEST_UPDATE_AMOUNT = BigDecimal.valueOf(150);
    private static final LocalDate TEST_DATE = FIXED_DATE;
    private static final ExpenseCategory TEST_CATEGORY = ExpenseCategory.HOUSING;
    private static final ExpenseCategory TEST_CATEGORY_UPDATE = ExpenseCategory.ENTERTAINMENT; ;

    @Test
    void testGetExpenseCategories() {
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

        AddExpenseRequest request = new AddExpenseRequest(
            FIXED_DATE,
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
        String authToken = authTokenForTestUser();

        String requestJson = """
            {
                "amount": null,
                "category": "FOOD",
                "date": "%s"
            }
            """.formatted(FIXED_DATE);

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
        String authToken = authTokenForTestUser();

        String requestJson = """
            {
                "amount": 25.50,
                "category": null,
                "date": "%s"
            }
            """.formatted(FIXED_DATE);

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
        String authToken = authTokenForTestUser();

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
        String authToken = authTokenForTestUser();

        String requestJson = """
            {
                "amount": 25.50,
                "category": "INVALID_CATEGORY",
                "date": "%s"
            }
            """.formatted(FIXED_DATE);

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
            FIXED_DATE,
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
        String authToken = authTokenForTestUser();

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
            FIXED_DATE,
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
            .when()
            .get(EXPENSES_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().body().asString();

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
        String authToken = authTokenForTestUser();

        LocalDate fromDate = FIXED_DATE.minusMonths(1);
        LocalDate toDate = FIXED_DATE;

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
            FIXED_DATE.minusDays(15), // Date between fromDate and toDate
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

        String authToken = authTokenForTestUser();

        AddExpenseRequest expenseRequest = new AddExpenseRequest(
            FIXED_DATE,
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
        String authToken = authTokenForTestUser();

        AddExpenseRequest createRequest = new AddExpenseRequest(
            FIXED_DATE,
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
            .statusCode(400);
    }

    @Test
    void testUpdateExpenseOfAnotherUser() {
        String firstUserToken = authTokenForTestUser();

        AddExpenseRequest createRequest = new AddExpenseRequest(
            FIXED_DATE,
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

        String secondUserToken = authTokenForSecondTestUser();

        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
            FIXED_DATE,
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
        String authToken = authTokenForTestUser();

        AddExpenseRequest createRequest = new AddExpenseRequest(
            FIXED_DATE,
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

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(EXPENSES_ENDPOINT + "/" + expenseId)
            .then()
            .statusCode(204);

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

    @Test
    void testSuccessfulExpenseUpdate() {
        String authToken = authTokenForTestUser();

        AddExpenseRequest createRequest = new AddExpenseRequest(
                TEST_DATE,
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

        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                TEST_DATE,
                TEST_UPDATE_AMOUNT,
                TEST_CATEGORY_UPDATE
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when()
                .put(EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(200)
                .body("id", equalTo(expenseId.intValue()))
                .body("amount", equalTo(150))
                .body("category", equalTo(TEST_CATEGORY_UPDATE.name()))
                .body("date", notNullValue());
    }

    @Test
    void testUpdateNonExistentExpense() {
        String authToken = authTokenForTestUser();

        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                TEST_DATE,
                TEST_UPDATE_AMOUNT,
                TEST_CATEGORY_UPDATE
        );

        // Use a non-existent ID (assuming 999999L doesn't exist)
        Long nonExistentId = 999999L;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(updateRequest)
                .when()
                .put(EXPENSES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateExpenseWithoutAuthentication() {
        UpdateExpenseRequest updateRequest = new UpdateExpenseRequest(
                TEST_DATE,
                TEST_UPDATE_AMOUNT,
                TEST_CATEGORY_UPDATE
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(EXPENSES_ENDPOINT + "/1") // Any ID would work for this test
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDeleteExpenseWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(EXPENSES_ENDPOINT + "/1") // Any ID would work for this test
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDeleteNonExistentExpense() {
        String authToken = authTokenForTestUser();

        // Use a non-existent ID (assuming 999999L doesn't exist)
        Long nonExistentId = 999999L;

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete(EXPENSES_ENDPOINT + "/" + nonExistentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteExpenseOfAnotherUser() {
        String firstUserToken = authTokenForTestUser();

        AddExpenseRequest createRequest = new AddExpenseRequest(
                FIXED_DATE,
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

        String secondUserToken = authTokenForSecondTestUser();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + secondUserToken)
                .when()
                .delete(EXPENSES_ENDPOINT + "/" + expenseId)
                .then()
                .statusCode(404);
    }

    @Test
    void testGetExpensesWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(401)
                .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetExpensesWithInvalidCategory() {
        String authToken = authTokenForTestUser();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("category", "INVALID_CATEGORY")
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(500);
    }

    @Test
    void testGetExpensesWithInvalidDateFormat() {
        String authToken = authTokenForTestUser();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .queryParam("from_date", "invalid-date")
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(500);
    }

    @Test
    void testInvalidJWTToken() {
        String invalidToken = "invalid.jwt.token";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .get(EXPENSES_ENDPOINT)
                .then()
                .statusCode(401);
    }
}

