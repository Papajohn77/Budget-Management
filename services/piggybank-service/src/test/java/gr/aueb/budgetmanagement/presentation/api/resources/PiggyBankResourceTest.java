package gr.aueb.budgetmanagement.presentation.api.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.IntegrationBase;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.presentation.api.requests.AllocateToPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.AuthenticateUserRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupPiggyBankRequest;
import gr.aueb.budgetmanagement.presentation.api.requests.CreatePersonalPiggyBankRequest;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@QuarkusTest
class PiggyBankResourceTest extends IntegrationBase {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final String PIGGY_BANKS_ENDPOINT = "/api/v1/piggy-banks";
    private static final String LOGIN_ENDPOINT = "/api/v1/users/login";
    private static final String EXISTING_EMAIL = "test@example.com"; // From test fixture
    private static final String EXISTING_PASSWORD = "Test123!@#"; // From test fixture

    @Test
    void testSuccessfulPersonalPiggyBankCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);
    
        CreatePersonalPiggyBankRequest request = new CreatePersonalPiggyBankRequest(
            "Test Piggy Bank",
            new BigDecimal("100.00"),
            ExpenseCategory.ENTERTAINMENT
        );
    
        // Extract the response to inspect its structure
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201);
        
        // Use the correct JSON path based on the actual response structure
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Piggy Bank"))
            .body("target_amount", equalTo(100.00f))
            .body("category", equalTo("ENTERTAINMENT"));
    }

    @Test
    void testPersonalPiggyBankCreationWithoutAuthentication() {
        CreatePersonalPiggyBankRequest request = new CreatePersonalPiggyBankRequest(
            "Test Piggy Bank",
            new BigDecimal("100.00"),
            ExpenseCategory.ENTERTAINMENT
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testSuccessfulPersonalPiggyBankDeletion() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create a piggy bank first
        CreatePersonalPiggyBankRequest createRequest = new CreatePersonalPiggyBankRequest(
            "Piggy Bank To Delete",
            new BigDecimal("100.00"),
            ExpenseCategory.ENTERTAINMENT
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Then delete it
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId)
            .then()
            .statusCode(204);
    }

    @Test
    void testDeletingNonExistentPiggyBank() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/999") // Non-existent ID
            .then()
            .statusCode(404)
            .body("message", containsString("Piggy bank not found"));
    }

    @Test
    void testDeletingPiggyBankWithoutAuthentication() {
        long piggyBankId = Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID;

        given()
            .contentType(ContentType.JSON)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/"+ piggyBankId)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDeletingOtherUsersPiggyBank() {
        // Login as testuser
        AuthenticateUserRequest loginRequest1 = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken1 = getAuthTokenAuthenticate(loginRequest1);

        // Create a piggy bank as testuser
        CreatePersonalPiggyBankRequest createRequest = new CreatePersonalPiggyBankRequest(
            "Piggy Bank For Auth Test",
            new BigDecimal("100.00"),
            ExpenseCategory.ENTERTAINMENT
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken1)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Login as testuser2
        AuthenticateUserRequest loginRequest2 = new AuthenticateUserRequest(
            "test2@example.com", // Another user from test fixture
            EXISTING_PASSWORD
        );
        String authToken2 = getAuthTokenAuthenticate(loginRequest2);

        // Try to delete testuser's piggy bank as testuser2
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken2)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId)
            .then()
            .statusCode(403)
            .body("message", containsString("User is not authorized to dissolve this piggy bank"));
    }
    
    @Test
    void testSuccessfulGroupPiggyBankCreation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        CreateGroupPiggyBankRequest request = new CreateGroupPiggyBankRequest(
            "Group Piggy Bank",
            new BigDecimal("200.00"),
            ExpenseCategory.FOOD
        );

        Long groupId = Fixture.Groups.TESTGROUP_ID; // Using the test group from fixture

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/groups/" + groupId + "/piggy-banks")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Group Piggy Bank"))
            .body("target_amount", equalTo(200.00f))
            .body("category", equalTo("FOOD"))
            .body("group_id", equalTo(groupId.intValue()));
    }

    @Test
    void testGroupPiggyBankCreationByNonAdmin() {
        // Login as non-admin user
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            "test3@example.com", // Non-admin user from test fixture
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        CreateGroupPiggyBankRequest request = new CreateGroupPiggyBankRequest(
            "Group Piggy Bank",
            new BigDecimal("200.00"),
            ExpenseCategory.FOOD
        );

        Long groupId = Fixture.Groups.TESTGROUP_ID; // Using the test group from fixture

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/groups/" + groupId + "/piggy-banks")
            .then()
            .statusCode(403)
            .body("message", containsString("Only group admin can create group piggy banks"));
    }

    @Test
    void testSuccessfulGroupPiggyBankDeletion() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create a group piggy bank first
        CreateGroupPiggyBankRequest createRequest = new CreateGroupPiggyBankRequest(
            "Group Piggy Bank To Delete",
            new BigDecimal("200.00"),
            ExpenseCategory.FOOD
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Then delete it
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId)
            .then()
            .statusCode(204);
    }

    @Test
    void testGroupPiggyBankDeletionByNonAdmin() {
        // Login as admin to create the piggy bank
        AuthenticateUserRequest adminLoginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String adminAuthToken = getAuthTokenAuthenticate(adminLoginRequest);

        // Create a group piggy bank as admin
        CreateGroupPiggyBankRequest createRequest = new CreateGroupPiggyBankRequest(
            "Group Piggy Bank For Auth Test",
            new BigDecimal("200.00"),
            ExpenseCategory.FOOD
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + adminAuthToken)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Login as non-admin
        AuthenticateUserRequest nonAdminLoginRequest = new AuthenticateUserRequest(
            "test3@example.com", // Non-admin user from test fixture
            EXISTING_PASSWORD
        );
        String nonAdminAuthToken = getAuthTokenAuthenticate(nonAdminLoginRequest);

        // Try to delete as non-admin
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + nonAdminAuthToken)
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId)
            .then()
            .statusCode(403)
            .body("message", containsString("User is not authorized to dissolve this piggy bank"));
    }    

    @Test
    void testCreateGroupPiggyBankWithoutAuthentication() {
        CreateGroupPiggyBankRequest request = new CreateGroupPiggyBankRequest(
            "Group Piggy Bank",
            new BigDecimal("200.00"),
            ExpenseCategory.FOOD
        );

        Long groupId = Fixture.Groups.TESTGROUP_ID;

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/groups/" + groupId + "/piggy-banks")
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testDeleteGroupPiggyBankWithoutAuthentication() {
        Long piggyBankId = Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID;

        given()
            .when()
            .delete(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testSuccessfulAllocationToPiggyBank() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Create a personal piggy bank first
        CreatePersonalPiggyBankRequest createRequest = new CreatePersonalPiggyBankRequest(
            "Piggy Bank For Allocation",
            new BigDecimal("300.00"),
            ExpenseCategory.ENTERTAINMENT
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Then allocate funds to it
        AllocateToPiggyBankRequest allocateRequest = new AllocateToPiggyBankRequest(
            FIXED_DATE,
            new BigDecimal("50.00")
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId + "/allocations")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("amount", equalTo(50.00f))
            .body("piggy_bank_id", equalTo(piggyBankId.intValue()));
    }

    @Test
    void testAllocationToNonExistentPiggyBank() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        AllocateToPiggyBankRequest allocateRequest = new AllocateToPiggyBankRequest(
            FIXED_DATE,
            new BigDecimal("50.00")
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/999/allocations") // Non-existent ID
            .then()
            .statusCode(404)
            .body("message", containsString("Piggy bank not found"));
    }

    @Test
    void testAllocationToOtherUsersPiggyBank() {
        // Login as testuser
        AuthenticateUserRequest loginRequest1 = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken1 = getAuthTokenAuthenticate(loginRequest1);

        // Create a piggy bank as testuser
        CreatePersonalPiggyBankRequest createRequest = new CreatePersonalPiggyBankRequest(
            "Piggy Bank For Auth Test",
            new BigDecimal("300.00"),
            ExpenseCategory.ENTERTAINMENT
        );

        Long piggyBankId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken1)
            .body(createRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Login as testuser2
        AuthenticateUserRequest loginRequest2 = new AuthenticateUserRequest(
            "test2@example.com", // Another user from test fixture
            EXISTING_PASSWORD
        );
        String authToken2 = getAuthTokenAuthenticate(loginRequest2);

        // Try to allocate to testuser's piggy bank as testuser2
        AllocateToPiggyBankRequest allocateRequest = new AllocateToPiggyBankRequest(
            FIXED_DATE,
            new BigDecimal("50.00")
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken2)
            .body(allocateRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/" + piggyBankId + "/allocations")
            .then()
            .statusCode(403)
            .body("message", containsString("User is not authorized"));
    }

    @Test
    void testAllocateToExistingPiggyBankWithAllocation() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        // Allocate additional funds to the piggy bank that already has an allocation
        AllocateToPiggyBankRequest allocateRequest = new AllocateToPiggyBankRequest(
            FIXED_DATE,
            new BigDecimal("50.00")
        );

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(allocateRequest)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/1/allocations") // ID of testpersonalpiggy
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("amount", notNullValue())
            .body("piggy_bank_id", equalTo(1));
    }

    @Test
    void testAllocateToPiggyBankWithoutAuthentication() {
        AllocateToPiggyBankRequest request = new AllocateToPiggyBankRequest(
            FIXED_DATE,
            new BigDecimal("50.00")
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post(PIGGY_BANKS_ENDPOINT + "/1/allocations")
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
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
    void testGetPiggyBanks() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);
    
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("personal_piggy_banks", notNullValue())
            .body("personal_piggy_banks.size()", is(1))
            .body("personal_piggy_banks[0].id", equalTo(1))
            .body("personal_piggy_banks[0].name", equalTo("testpersonalpiggy"))
            .body("personal_piggy_banks[0].target_amount", equalTo(1000.00f))
            .body("personal_piggy_banks[0].category", equalTo("ENTERTAINMENT"))
            .body("group_piggy_banks", notNullValue())
            .body("group_piggy_banks.size()", is(1))
            .body("group_piggy_banks[0].name", equalTo("testgroup"))
            .body("group_piggy_banks[0].group_id", equalTo(1))
            .body("group_piggy_banks[0].piggy_banks", notNullValue())
            .body("group_piggy_banks[0].piggy_banks.size()", is(1))
            .body("group_piggy_banks[0].piggy_banks[0].id", equalTo(2))
            .body("group_piggy_banks[0].piggy_banks[0].name", equalTo("testgrouppiggy"))
            .body("group_piggy_banks[0].piggy_banks[0].target_amount", equalTo(2000.00f))
            .body("group_piggy_banks[0].piggy_banks[0].category", equalTo("ENTERTAINMENT"))
            .body("group_piggy_banks[0].piggy_banks[0].group_id", equalTo(1));
    }

    @Test
    void testGetPiggyBanksWithoutAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(401)
            .body("message", containsString("Missing Authorization header"));
    }

    @Test
    void testGetPersonalPiggyBanksOnly() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("type", "personal")
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("personal_piggy_banks", notNullValue())
            .body("personal_piggy_banks.size()", is(1))
            .body("group_piggy_banks", notNullValue())
            .body("group_piggy_banks.size()", is(0));
    }

    @Test
    void testGetGroupPiggyBanksOnly() {
        AuthenticateUserRequest loginRequest = new AuthenticateUserRequest(
            EXISTING_EMAIL,
            EXISTING_PASSWORD
        );
        String authToken = getAuthTokenAuthenticate(loginRequest);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("type", "group")
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("personal_piggy_banks", notNullValue())
            .body("personal_piggy_banks.size()", is(0))
            .body("group_piggy_banks", notNullValue())
            .body("group_piggy_banks.size()", is(1));
    }
}
