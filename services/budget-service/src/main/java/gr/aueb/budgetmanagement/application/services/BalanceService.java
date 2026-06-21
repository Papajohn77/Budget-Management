package gr.aueb.budgetmanagement.application.services;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import gr.aueb.budgetmanagement.application.clients.PiggyBankClient;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.BalanceRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class BalanceService {
    private final UserRepository userRepository;
    private final PiggyBankClient piggyBankClient;

    public BalanceService(
        UserRepository userRepository,
        @RestClient PiggyBankClient piggyBankClient
    ) {
        this.userRepository = userRepository;
        this.piggyBankClient = piggyBankClient;
    }

    @Transactional
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 3, delayUnit = ChronoUnit.SECONDS, failOn = {WebApplicationException.class, TimeoutException.class, ProcessingException.class})
    public BalanceRepresentation getBalance(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        BigDecimal localBalance = user.getCurrentBalance();

        PiggyBankTotalsRepresentation piggyBankTotals;
        try {
            piggyBankTotals = piggyBankClient.getTotals(userId);
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 401) {
                throw new InvalidCredentialsException("Unauthorized when calling Piggy Bank service");
            }
            if (status == 404) {
                throw new NotFoundException("User not found with id: " + userId);
            }
            throw e;
        }

        BigDecimal balance = localBalance.subtract(piggyBankTotals.total());

        return new BalanceRepresentation(balance);
    }
}
