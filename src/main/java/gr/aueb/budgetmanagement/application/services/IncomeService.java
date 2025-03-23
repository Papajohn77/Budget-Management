package gr.aueb.budgetmanagement.application.services;

import java.time.LocalDate;
import java.util.List;

import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedIncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.IncomesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Income;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class IncomeService {
    private final UserRepository userRepository;

    public IncomeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddedIncomeRepresentation createIncome(@Valid AddIncomeCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        Income income = user.addIncome(
            command.amount(),
            command.date(),
            command.category()
        );

        userRepository.save(user);

        return new AddedIncomeRepresentation(
            income.getId(),
            income.getAmount().getValue(),
            income.getDate(),
            income.getCategory()
        );
    }

    @Transactional
    public AddedIncomeRepresentation updateIncome(@Valid UpdateIncomeCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + command.userId()));

        Income income = user.getIncomes().stream()
            .filter(e -> e.getId().equals(command.incomeId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Income not found with id: " + command.incomeId()));

        income.update(command.amount(), command.date(), command.category());

        userRepository.save(user);

        return new AddedIncomeRepresentation(
            income.getId(),
            income.getAmount().getValue(),
            income.getDate(),
            income.getCategory()
        );
    }

    @Transactional
    public void deleteIncome(Long incomeId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        user.removeIncome(incomeId);

        userRepository.save(user);
    }

    public IncomesRepresentation getIncomes(Long userId, LocalDate fromDate, LocalDate toDate, IncomeCategory category) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Income> incomes = user.getIncomes().stream()
            .filter(e -> fromDate == null || !e.getDate().isBefore(fromDate))
            .filter(e -> toDate == null || !e.getDate().isAfter(toDate))
            .filter(e -> category == null || e.getCategory() == category)
            .toList();

        return new IncomesRepresentation(toAddedIncomeRepresentationList(incomes));
    }

    private List<AddedIncomeRepresentation> toAddedIncomeRepresentationList(List<Income> incomes) {
        return incomes.stream()
            .map(this::toAddedIncomeRepresentation)
            .toList();
    }

    private AddedIncomeRepresentation toAddedIncomeRepresentation(Income income) {
        return new AddedIncomeRepresentation(
            income.getId(),
            income.getAmount().getValue(),
            income.getDate(),
            income.getCategory()
        );
    }
}
