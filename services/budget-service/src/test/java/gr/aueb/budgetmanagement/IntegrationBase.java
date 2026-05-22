package gr.aueb.budgetmanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

public class IntegrationBase {
    @Inject
    private EntityManager entityManager;

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
