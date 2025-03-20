package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.RecurringIncomesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecurringIncomeService {
    private final UserRepository userRepository;

    public RecurringIncomeService(
        UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddedRecurringIncomeRepresentation createRecurringIncome(
        @Valid AddRecurringIncomeCommand command
    ) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringIncome recurringIncome = user.addRecurringIncome(
            command.name(),
            command.amount(),
            command.category(),
            command.startDate(),
            command.endDate()
        );

        userRepository.save(user);

        return toAddedRecurringIncomeRepresentation(recurringIncome);
    }

    public RecurringIncomesRepresentation getRecurringIncomes(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        List<RecurringIncome> recurringIncomes = new ArrayList<>(user.getRecurringIncomes());

        return new RecurringIncomesRepresentation(
            toAddedRecurringIncomeRepresentationList(recurringIncomes)
        );
    }

    @Transactional
    public void updateRecurringIncome(@Valid UpdateRecurringIncomeCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringIncome recurringIncome = user.getRecurringIncomes().stream()
            .filter(re -> re.getId().equals(command.recurringIncomeId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Recurring income not found"));

        recurringIncome.stop(command.isStopped());

        userRepository.save(user);
    }

    @Transactional
    public void deleteRecurringIncome(Long recurringIncomeId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        RecurringIncome recurringIncome = user.getRecurringIncomes().stream()
            .filter(re -> re.getId().equals(recurringIncomeId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Recurring income not found"));

        user.removeRecurringIncome(recurringIncomeId);

        userRepository.save(user);
    }

    private List<AddedRecurringIncomeRepresentation> toAddedRecurringIncomeRepresentationList(List<RecurringIncome> recurringIncomes) {
        return recurringIncomes.stream()
            .map(this::toAddedRecurringIncomeRepresentation)
            .toList();
    }

    private AddedRecurringIncomeRepresentation toAddedRecurringIncomeRepresentation(RecurringIncome recurringIncome) {
        return new AddedRecurringIncomeRepresentation(
            recurringIncome.getId(),
            recurringIncome.getName(),
            recurringIncome.getAmount().getValue(),
            recurringIncome.getCategory(),
            recurringIncome.getStartDate(),
            recurringIncome.getEndDate(),
            recurringIncome.getLastAppliedDate(),
            recurringIncome.isStopped()
        );
    }
}

