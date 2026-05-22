package gr.aueb.budgetmanagement.presentation.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class RootResourceTest {
    
    @Test
    void testRootRedirectsToSwagger() {
        given()
            .redirects().follow(false)
            .when()
            .get("/")
            .then()
            .statusCode(307) // Temporary redirect
            .header("Location", containsString("/q/swagger-ui"));
    }
}
