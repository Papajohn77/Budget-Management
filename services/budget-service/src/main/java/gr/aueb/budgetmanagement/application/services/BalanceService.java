package gr.aueb.budgetmanagement.application.services;

import java.math.BigDecimal;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import gr.aueb.budgetmanagement.application.clients.PiggyBankClient;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.BalanceRepresentation;
import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

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
    public BalanceRepresentation getBalance(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        BigDecimal localBalance = user.getCurrentBalance();

        PiggyBankTotalsRepresentation piggyBankTotals = piggyBankClient.getTotals(userId);

        BigDecimal balance = localBalance.subtract(piggyBankTotals.total());

        return new BalanceRepresentation(balance);
    }
}
