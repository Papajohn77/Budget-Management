package gr.aueb.budgetmanagement.presentation.api.filters;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.IntegrationBase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class EnsureUserShadowFilterTest extends IntegrationBase {
    private static final String PIGGY_BANKS_ENDPOINT = "/api/v1/piggy-banks";
    private static final Long UNKNOWN_USER_ID = 9999L;

    @Inject
    EntityManager entityManager;

    @Test
    void authenticatedRequestForUnknownUserCreatesShadowUser() {
        assertEquals(0L, countUsers(UNKNOWN_USER_ID));

        String token = authTokenFor(UNKNOWN_USER_ID);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(200);

        assertEquals(1L, countUsers(UNKNOWN_USER_ID));
    }

    @Test
    void unauthenticatedRequestDoesNotCreateShadowUser() {
        assertEquals(0L, countUsers(UNKNOWN_USER_ID));

        given()
            .contentType(ContentType.JSON)
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(401);

        assertEquals(0L, countUsers(UNKNOWN_USER_ID));
    }

    @Test
    void authenticatedRequestForExistingUserDoesNotDuplicateUser() {
        assertEquals(1L, countUsers(1L));

        String token = authTokenFor(1L);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .get(PIGGY_BANKS_ENDPOINT)
            .then()
            .statusCode(200);

        assertEquals(1L, countUsers(1L));
    }

    private long countUsers(Long userId) {
        Number count = (Number) entityManager
            .createNativeQuery("SELECT COUNT(*) FROM USERS WHERE ID = :id")
            .setParameter("id", userId)
            .getSingleResult();
        return count.longValue();
    }
}
