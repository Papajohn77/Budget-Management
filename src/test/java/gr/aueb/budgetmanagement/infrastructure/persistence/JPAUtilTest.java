package gr.aueb.budgetmanagement.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

class JPAUtilTest {
    @Test
    void testGetCurrentEntityManager() {
        EntityManager em = JPAUtil.getCurrentEntityManager();
        assertNotNull(em);
        assumeTrue(em.isOpen());
        em.close();
    }
}
