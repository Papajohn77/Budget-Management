package gr.aueb.budgetmanagement.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Base64;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class JwtTokenServiceTest {
    @Inject
    private UserRepository userRepository;

    @Inject
    private JwtTokenService jwtTokenService;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String expectedIssuer;

    @ConfigProperty(name = "mp.jwt.verify.token.age")
    int expectedTokenAge;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testGenerateToken() throws JsonProcessingException {
        // Act
        String token = jwtTokenService.generateToken(user);
        
        // Verify token is not empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse and verify token contents
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
        
        // Decode the payload (second part)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode claims = mapper.readTree(payload);
        
        // Verify claims
        assertEquals(user.getEmail().getValue(), claims.get("sub").asText());
        assertEquals(user.getId().toString(), claims.get("user_id").asText());
        assertEquals(expectedIssuer, claims.get("iss").asText());

        // Verify expiration
        long issuedAt = claims.get("iat").asLong();
        long expiresAt = claims.get("exp").asLong();
        long durationSeconds = expiresAt - issuedAt;
        assertEquals(expectedTokenAge, durationSeconds);
    }
}
