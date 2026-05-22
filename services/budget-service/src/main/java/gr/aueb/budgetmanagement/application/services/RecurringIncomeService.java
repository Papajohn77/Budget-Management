package gr.aueb.budgetmanagement.application.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.RecurringIncomeRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.RecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.RecurringIncomesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Income;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class RecurringIncomeService {
    private final UserRepository userRepository;
    private final RecurringIncomeRepository recurringIncomeRepository;

    public RecurringIncomeService(
        UserRepository userRepository,
        RecurringIncomeRepository recurringIncomeRepository
    ) {
        this.userRepository = userRepository;
        this.recurringIncomeRepository = recurringIncomeRepository;
    }

    @Transactional
    public RecurringIncomeRepresentation createRecurringIncome(
        @Valid AddRecurringIncomeCommand command
    ) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

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

    @Transactional
    public void updateRecurringIncome(@Valid UpdateRecurringIncomeCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        RecurringIncome recurringIncome = user.getRecurringIncomes().stream()
            .filter(re -> re.getId().equals(command.recurringIncomeId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Recurring income with id: " + command.recurringIncomeId() 
                + " was not found for user with id: " + command.userId()
            ));

        recurringIncome.stop(command.isStopped());

        userRepository.save(user);
    }

    @Transactional
    public void deleteRecurringIncome(Long recurringIncomeId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        user.getRecurringIncomes().stream()
            .filter(re -> re.getId().equals(recurringIncomeId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Recurring income with id: " + recurringIncomeId 
                + " was not found for user with id: " + userId
            ));

        user.removeRecurringIncome(recurringIncomeId);

        userRepository.save(user);
    }

    @Transactional
    public RecurringIncomesRepresentation getRecurringIncomes(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        List<RecurringIncome> recurringIncomes = new ArrayList<>(user.getRecurringIncomes());

        return new RecurringIncomesRepresentation(
            toAddedRecurringIncomeRepresentationList(recurringIncomes)
        );
    }

    @Transactional
    public void applyRecurringIncomes(LocalDate currentDate) {
        List<RecurringIncome> activeRecurringIncomes = recurringIncomeRepository.findNonStoppedRecurringIncomes();
        for (RecurringIncome recurringIncome : activeRecurringIncomes) {
            Income generatedIncome = recurringIncome.apply(currentDate);
            if (generatedIncome != null) {
                recurringIncomeRepository.save(recurringIncome);
            }
        }
    }

    private List<RecurringIncomeRepresentation> toAddedRecurringIncomeRepresentationList(List<RecurringIncome> recurringIncomes) {
        return recurringIncomes.stream()
            .map(this::toAddedRecurringIncomeRepresentation)
            .toList();
    }

    private RecurringIncomeRepresentation toAddedRecurringIncomeRepresentation(RecurringIncome recurringIncome) {
        return new RecurringIncomeRepresentation(
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
