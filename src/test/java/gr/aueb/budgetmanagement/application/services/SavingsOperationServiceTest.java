package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.AllocateSavingsCommand;
import gr.aueb.budgetmanagement.application.commands.DeallocateSavingsCommand;
import gr.aueb.budgetmanagement.application.dto.SavingsOperationDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.repositories.SavingsOperationRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaSavingsOperationRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class SavingsOperationServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final LocalDate TODAY = LocalDate.now();
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private SavingsOperationRepository savingsOperationRepository;
    private SavingsOperationService savingsOperationService;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        savingsOperationRepository = new JpaSavingsOperationRepository(entityManager);
        savingsOperationService = new SavingsOperationService(userRepository, savingsOperationRepository);

        createTestUser();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    private void createTestUser() {
        user = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        entityManager.persist(user);
    }

    @Test
    void testSuccessfulAllocation() {
        AllocateSavingsCommand command = new AllocateSavingsCommand(
            user.getId(),
            AMOUNT,
            TODAY
        );

        SavingsOperationDTO result = savingsOperationService.allocate(command);

        assertNotNull(result.id());
        assertEquals(AMOUNT, result.amount());
        assertEquals(TODAY, result.date());
        assertEquals(SavingsOperationType.ALLOCATION, result.operationType());
        assertEquals(user.getSavings().getId(), result.savingsId());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(AMOUNT, persistedUser.getSavings().getCurrentAmount().getValue());
        assertEquals(1, persistedUser.getSavings().getOperations().size());
    }

    @Test
    void testAllocationWithNonexistentUser() {
        AllocateSavingsCommand command = new AllocateSavingsCommand(
            999L,
            AMOUNT,
            TODAY
        );

        assertThrows(
            NotFoundException.class,
            () -> savingsOperationService.allocate(command)
        );
    }

    @Test
    void testSuccessfulDeallocation() {
        // First allocate
        savingsOperationService.allocate(
            new AllocateSavingsCommand(user.getId(), AMOUNT, TODAY)
        );

        // Then deallocate half
        BigDecimal deallocationAmount = AMOUNT.divide(new BigDecimal("2"));
        DeallocateSavingsCommand command = new DeallocateSavingsCommand(
            user.getId(),
            deallocationAmount,
            TODAY
        );

        SavingsOperationDTO result = savingsOperationService.deallocate(command);

        assertNotNull(result.id());
        assertEquals(deallocationAmount, result.amount());
        assertEquals(TODAY, result.date());
        assertEquals(SavingsOperationType.DEALLOCATION, result.operationType());
        assertEquals(user.getSavings().getId(), result.savingsId());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(deallocationAmount, persistedUser.getSavings().getCurrentAmount().getValue());
        assertEquals(2, persistedUser.getSavings().getOperations().size());
    }

    @Test
    void testDeallocationWithNonexistentUser() {
        DeallocateSavingsCommand command = new DeallocateSavingsCommand(
            999L,
            AMOUNT,
            TODAY
        );

        assertThrows(
            NotFoundException.class,
            () -> savingsOperationService.deallocate(command)
        );
    }

    @Test
    void testDeallocationWithInsufficientFunds() {
        // First allocate
        savingsOperationService.allocate(
            new AllocateSavingsCommand(user.getId(), AMOUNT, TODAY)
        );

        // Try to deallocate more than allocated
        DeallocateSavingsCommand command = new DeallocateSavingsCommand(
            user.getId(),
            AMOUNT.add(BigDecimal.ONE),
            TODAY
        );

        assertThrows(
            InsufficientSavingsException.class,
            () -> savingsOperationService.deallocate(command)
        );

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(AMOUNT, persistedUser.getSavings().getCurrentAmount().getValue());
        assertEquals(1, persistedUser.getSavings().getOperations().size());
    }
}
