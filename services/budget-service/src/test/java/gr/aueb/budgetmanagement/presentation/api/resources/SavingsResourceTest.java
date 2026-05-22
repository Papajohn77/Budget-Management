package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.RegisterUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.SavingsOperationRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class SavingsResourceTest extends IntegrationBase {
    private static final String SAVINGS_ENDPOINT = "/api/v1/savings";
    private static final String ALLOCATIONS_ENDPOINT = "/api/v1/savings/allocations";
    private static final String DEALLOCATIONS_ENDPOINT = "/api/v1/savings/deallocations";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/users/register";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String TEST_PASSWORD = "Test123!@#"; // From test fixture
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100.00");
    private static final LocalDate TEST_DATE = LocalDate.now();
    
    @Test
    void testSuccessfulAllocation() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("date", equalTo(TEST_DATE.toString()))
            .body("amount", equalTo(TEST_AMOUNT.floatValue()))
            .body("operation_type", equalTo("ALLOCATION"))
            .body("savings_id", notNullValue());
    }
    
    @Test
    void testAllocationWithoutAuthentication() {
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }
    
    @Test
    void testAllocationWithInvalidToken() {
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .body(request)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(401);
    }
    
    @Test
    void testAllocationWithNegativeAmount() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            new BigDecimal("-50.00")
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Money cannot be negative"));
    }
    
    @Test
    void testAllocationWithNullDate() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        SavingsOperationRequest request = new SavingsOperationRequest(
            null,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Date is required."));
    }
    
    @Test
    void testSuccessfulDeallocation() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        // First allocate some money
        SavingsOperationRequest allocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            new BigDecimal("150.00")
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201);
            
        // Then deallocate part of it
        SavingsOperationRequest deallocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(deallocateRequest)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("date", equalTo(TEST_DATE.toString()))
            .body("amount", equalTo(TEST_AMOUNT.floatValue()))
            .body("operation_type", equalTo("DEALLOCATION"))
            .body("savings_id", notNullValue());
    }
    
    @Test
    void testDeallocationWithoutAuthentication() {
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }
    
    @Test
    void testDeallocationWithInvalidToken() {
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .body(request)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(401);
    }
    
    @Test
    void testDeallocationWithNegativeAmount() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        SavingsOperationRequest request = new SavingsOperationRequest(
            TEST_DATE,
            new BigDecimal("-50.00")
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Money cannot be negative"));
    }
    
    @Test
    void testDeallocationWithNullDate() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        SavingsOperationRequest request = new SavingsOperationRequest(
            null,
            TEST_AMOUNT
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Date is required."));
    }
    
    @Test
    void testDeallocationWithInsufficientFunds() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );
        
        // First allocate a small amount
        SavingsOperationRequest allocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            new BigDecimal("50.00")
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201);
            
        // Then try to deallocate more than what's available
        SavingsOperationRequest deallocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            new BigDecimal("1000.00")
        );
        
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(deallocateRequest)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(400)
            .body("message", containsString("Insufficient savings balance"));
    }

    @Test
    void testGetSavingsForNewUser() {
        // Create and authenticate as a new user
        RegisterUserRequest registerRequest = new RegisterUserRequest(
            "savingsuser", 
            "savingsuser@example.com", 
            TEST_PASSWORD
        );

        String newUserToken = getAuthTokenRegister(registerRequest);

        // Get savings for new user (should start with zero)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + newUserToken)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("current_amount", equalTo(0));
    }

    @Test
    void testGetSavingsWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetSavingsWithInvalidToken() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(401);
    }

    @Test
    void testGetSavingsReflectsOperations() {
        String authToken = getAuthTokenAuthenticate(
            new AuthenticateUserRequest(EXISTING_EMAIL, TEST_PASSWORD)
        );

        // Get initial savings
        BigDecimal initialAmount = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().jsonPath().getObject("current_amount", BigDecimal.class);

        // Allocate money
        BigDecimal allocationAmount = new BigDecimal("99.99");
        SavingsOperationRequest allocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            allocationAmount
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(ALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201);

        // Get updated savings
        BigDecimal afterAllocationAmount = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().jsonPath().getObject("current_amount", BigDecimal.class);

        // Deallocate money
        BigDecimal deallocationAmount = new BigDecimal("50.50");
        SavingsOperationRequest deallocateRequest = new SavingsOperationRequest(
            TEST_DATE,
            deallocationAmount
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(deallocateRequest)
            .when()
            .post(DEALLOCATIONS_ENDPOINT)
            .then()
            .statusCode(201);

        // Get final savings
        BigDecimal finalAmount = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().jsonPath().getObject("current_amount", BigDecimal.class);

        // Verify changes
        assertEquals(initialAmount.add(allocationAmount), afterAllocationAmount);
        assertEquals(afterAllocationAmount.subtract(deallocationAmount), finalAmount);
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
