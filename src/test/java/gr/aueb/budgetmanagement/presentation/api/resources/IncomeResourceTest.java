package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.*;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class IncomeResourceTest extends IntegrationBase {
    private static final String INCOMES_ENDPOINT = "/api/v1/incomes";
    private static final String INCOME_CATEGORIES_ENDPOINT = "/api/v1/incomes/categories";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(53.75);
    private static final LocalDate TEST_DATE = LocalDate.now();
    private static final IncomeCategory TEST_CATEGORY = IncomeCategory.SALARY;
    private static final IncomeCategory TEST_CATEGORY_UPDATE = IncomeCategory.DIVIDENDS;

    @Test
    void testGetIncomeCategories() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );

        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
                EXISTING_EMAIL,
                EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        String requestJson = """
        {
            "amount": null,
            "category": "SALARY",
            "date": "%s"
        }
        """.formatted(LocalDate.now());

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
       AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(EXISTING_EMAIL, EXISTING_PASSWORD);

       String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(EXISTING_EMAIL, EXISTING_PASSWORD);
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }
    @Test
    void testGetIncomesWithCategoryFilter() {

        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );

        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddIncomeRequest incomeRequest = new AddIncomeRequest(
            LocalDate.now(),
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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddIncomeRequest createRequest = new AddIncomeRequest(
            LocalDate.now(),
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
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddIncomeRequest createRequest = new AddIncomeRequest(
            LocalDate.now(),
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

        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "otheruser",
                "otheruser@example.com",
                "Test123!@#"
        );

        String secondUserToken = getAuthTokenRegister(registerRequest);

        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest(
            LocalDate.now(),
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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AddIncomeRequest createRequest = new AddIncomeRequest(
            LocalDate.now(),
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
    void testSuccessfulIncomeUpdate() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest firstUserLogin = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String firstUserToken = getAuthTokenAuthenticate(firstUserLogin);

        AddIncomeRequest createRequest = new AddIncomeRequest(
            LocalDate.now(),
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

        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "otheruser3",
                "otheruser3@example.com",
                "Test123!@#"
        );

        String secondUserToken = getAuthTokenRegister(registerRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        LocalDate fromDate = LocalDate.now().minusMonths(1);
        LocalDate toDate = LocalDate.now();

        AddIncomeRequest incomeRequest = new AddIncomeRequest(
            LocalDate.now().minusDays(15), // Date between fromDate and toDate
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
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

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
            .post(INCOMES_ENDPOINT)
            .then()
            .statusCode(400);
    }
}
