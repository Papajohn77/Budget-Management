package gr.aueb.budgetmanagement.presentation.api.filters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class EnsureUserShadowFilterTest extends IntegrationBase {
    private static final String SAVINGS_ENDPOINT = "/api/v1/savings";
    private static final Long UNKNOWN_USER_ID = 9999L;

    @Inject
    EntityManager entityManager;

    @Test
    void authenticatedRequestForUnknownUserCreatesShadowUserAndSavings() {
        assertEquals(0L, countUsers(UNKNOWN_USER_ID));
        assertEquals(0L, countSavingsForUser(UNKNOWN_USER_ID));

        String token = authTokenFor(UNKNOWN_USER_ID);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get(SAVINGS_ENDPOINT)
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("current_amount", equalTo(0));

        assertEquals(1L, countUsers(UNKNOWN_USER_ID));
        assertEquals(1L, countSavingsForUser(UNKNOWN_USER_ID));
    }

    private long countUsers(Long userId) {
        Number count = (Number) entityManager
            .createNativeQuery("SELECT COUNT(*) FROM USERS WHERE ID = :id")
            .setParameter("id", userId)
            .getSingleResult();
        return count.longValue();
    }

    private long countSavingsForUser(Long userId) {
        Number count = (Number) entityManager
            .createNativeQuery("SELECT COUNT(*) FROM SAVINGS WHERE USER_ID = :id")
            .setParameter("id", userId)
            .getSingleResult();
        return count.longValue();
    }
}
