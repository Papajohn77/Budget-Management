package gr.aueb.budgetmanagement.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<>();

    private JPAUtil() {

    }

    private static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("budget-management");
        }
        return emf;
    }

    public static EntityManager getCurrentEntityManager() {      
        EntityManager em = currentEntityManager.get();         
        if (em  == null || !em.isOpen()) {
            em = getEntityManagerFactory().createEntityManager();
            currentEntityManager.set(em);
        }
        return em;
    }
}
