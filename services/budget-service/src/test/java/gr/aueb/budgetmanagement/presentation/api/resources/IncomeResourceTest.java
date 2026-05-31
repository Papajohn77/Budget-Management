package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AddIncomeRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.UpdateIncomeRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class IncomeResourceTest extends IntegrationBase {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final String INCOMES_ENDPOINT = "/api/v1/incomes";
    private static final String INCOME_CATEGORIES_ENDPOINT = "/api/v1/incomes/categories";    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(53.75);
    private static final LocalDate TEST_DATE = FIXED_DATE;
    private static final IncomeCategory TEST_CATEGORY = IncomeCategory.SALARY;
    private static final IncomeCategory TEST_CATEGORY_UPDATE = IncomeCategory.DIVIDENDS;

    @Test
    void testGetIncomeCategories() {
        String authToken = authTokenForTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(INCOME_CATEGORIES_ENDPOINT)
            .then()
            .statusCode(200)
            .body("income_categories", containsInAnyOrder(
                    Arrays.stream(IncomeCategory.values()).map(Enum::name).toArray(String[]::new)
            ));

    }
    @Test
    void testIncomeCreationWithNullCategory() {
        String authToken = authTokenForTestUser();

        // Create request with JSON that has null category
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
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }


    @Test
    void testIncomeCreationWithoutAuthentication() {

        AddIncomeRequest request = new AddIncomeRequest(
                TEST_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testIncomeCreationWithNullAmount() {
        String authToken = authTokenForTestUser();

        String requestJson = """
        {
            "amount": null,
            "category": "SALARY",
            "date": "%s"
        }
        """.formatted(FIXED_DATE);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(requestJson)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }

    @Test
    void testSuccessfulIncomeCreation() {
       String authToken = authTokenForTestUser();

        AddIncomeRequest request = new AddIncomeRequest(
                TEST_DATE,
                TEST_AMOUNT,
                TEST_CATEGORY
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("amount", equalTo(53.75f))
            .body("category", equalTo("SALARY"));
    }

    @Test
    void testGetIncomes() {
        String authToken = authTokenForTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(200)
            .body("incomes", notNullValue());
    }

    @Test
    void testIncomeCreationWithNullDate() {
        String authToken = authTokenForTestUser();

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
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }
    @Test
    void testGetIncomesWithCategoryFilter() {

        String authToken = authTokenForTestUser();

        AddIncomeRequest incomeRequest = new AddIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(incomeRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("category", TEST_CATEGORY.name())
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(200)
            .body("incomes", notNullValue())
            .body("incomes.size()", greaterThan(0))
            .body("incomes.findAll { it.category == '" + TEST_CATEGORY.name() + "' }.size()", greaterThan(0));
    }

    @Test
    void testUpdateExpenseWithInvalidData() {
        String authToken = authTokenForTestUser();

        AddIncomeRequest createRequest = new AddIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        Long expenseId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(INCOMES_ENDPOINT)
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
            .put(INCOMES_ENDPOINT + "/" + expenseId)
            .then()
            .statusCode(400);
    }

    @Test
    void testUpdateIncomeOfAnotherUser() {
        // Login as the first user and create an income
        String firstUserToken = authTokenForTestUser();

        AddIncomeRequest createRequest = new AddIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        Long expenseId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + firstUserToken)
            .body(createRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String secondUserToken = authTokenForSecondTestUser();

        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY_UPDATE
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + secondUserToken)
            .body(updateRequest)
            .when()
            .put(INCOMES_ENDPOINT + "/" + expenseId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteIncome() {
        String authToken = authTokenForTestUser();

        AddIncomeRequest createRequest = new AddIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        Long expenseId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(INCOMES_ENDPOINT + "/" + expenseId)
            .then()
            .statusCode(204);

        String response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().asString();

        assertFalse(response.contains("\"id\":" + expenseId + ","),
                "Response still contains the deleted expense ID");
    }

    @Test
    void testSuccessfulIncomeUpdate() {
        String authToken = authTokenForTestUser();

        AddIncomeRequest createRequest = new AddIncomeRequest(
            TEST_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        Long incomeId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        BigDecimal updatedAmount = BigDecimal.valueOf(150.0);
        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest(
            TEST_DATE,
            updatedAmount,
            TEST_CATEGORY_UPDATE
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(updateRequest)
            .when()
            .put(INCOMES_ENDPOINT + "/" + incomeId)
            .then()
            .statusCode(200)
            .body("id", equalTo(incomeId.intValue()))
            .body("amount", equalTo(150.0F))
            .body("category", equalTo(TEST_CATEGORY_UPDATE.name()))
            .body("date", notNullValue());
    }

    @Test
    void testUpdateNonExistentIncome() {
        String authToken = authTokenForTestUser();

        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest(
            TEST_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY_UPDATE
        );

        Long nonExistentId = 999999L;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(updateRequest)
            .when()
            .put(INCOMES_ENDPOINT + "/" + nonExistentId)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateIncomeWithoutAuthentication() {
        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest(
            TEST_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY_UPDATE
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put(INCOMES_ENDPOINT + "/1") // Any ID would work for this test
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
}

    @Test
    void testDeleteIncomeWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .delete(INCOMES_ENDPOINT + "/1") // Any ID would work for this test
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDeleteNonExistentIncome() {
        String authToken = authTokenForTestUser();

        Long nonExistentId = 999999L;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(INCOMES_ENDPOINT + "/" + nonExistentId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteIncomeOfAnotherUser() {
        String firstUserToken = authTokenForTestUser();

        AddIncomeRequest createRequest = new AddIncomeRequest(
            FIXED_DATE,
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        Long incomeId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + firstUserToken)
            .body(createRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        String secondUserToken = authTokenForSecondTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + secondUserToken)
            .when()
            .delete(INCOMES_ENDPOINT + "/" + incomeId)
            .then()
            .statusCode(404);
    }

    @Test
    void testGetIncomesWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetIncomesWithInvalidCategory() {
        String authToken = authTokenForTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("category", "INVALID_CATEGORY")
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(500);
    }

    @Test
    void testGetIncomesWithInvalidDateFormat() {
        String authToken = authTokenForTestUser();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("from_date", "invalid-date")
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(500);
    }

    @Test
    void testInvalidJWTTokenForIncomes() {
        String invalidToken = "invalid.jwt.token";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + invalidToken)
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(401);
    }

    @Test
    void testGetIncomesWithDateFilters() {
        String authToken = authTokenForTestUser();

        LocalDate fromDate = FIXED_DATE.minusMonths(1);
        LocalDate toDate = FIXED_DATE;

        AddIncomeRequest incomeRequest = new AddIncomeRequest(
            FIXED_DATE.minusDays(15), // Date between fromDate and toDate
            TEST_AMOUNT,
            TEST_CATEGORY
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(incomeRequest)
            .when()
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(201);

        // Test the filter
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("from_date", fromDate.toString())
            .queryParam("to_date", toDate.toString())
            .when()
            .get(INCOMES_ENDPOINT)
            .then()
            .statusCode(200)
            .body("incomes", notNullValue())
            .body("incomes.size()", greaterThan(0));
    }

    @Test
    void testIncomeCreationWithInvalidCategory() {
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
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }
}
