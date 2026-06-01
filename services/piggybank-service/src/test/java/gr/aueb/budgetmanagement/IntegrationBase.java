package gr.aueb.budgetmanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;

import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

public class IntegrationBase {
    private static final String JWT_ISSUER = "budget-management";

    @Inject
    private EntityManager entityManager;

    protected String authTokenFor(Long userId) {
        return Jwt.subject(userId.toString())
            .upn(userId.toString())
            .claim("user_id", userId)
            .issuer(JWT_ISSUER)
            .sign();
    }

    protected String authTokenForTestUser() {
        return authTokenFor(Fixture.Users.TESTUSER_ID);
    }

    protected String authTokenForSecondTestUser() {
        return authTokenFor(Fixture.Users.TESTUSER2_ID);
    }

    @BeforeEach
    @Transactional
    public void initDatabase() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("import.sql")) {
            String sql = convertStreamToString(in);
            entityManager.createNativeQuery(sql).executeUpdate();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    private String convertStreamToString(InputStream in) {
        try (Scanner scanner = new Scanner(in, "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
