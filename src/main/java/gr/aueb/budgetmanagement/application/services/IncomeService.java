package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedIncomeRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Income;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class IncomeService {
    private final UserRepository userRepository;

    public IncomeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddedIncomeRepresentation createIncome(@Valid AddIncomeCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        Income income = user.addIncome(
            command.amount(),
            command.date(),
            command.category()
        );

        userRepository.save(user);

        return new AddedIncomeRepresentation(
            income.getId(),
            income.getAmount(),
            income.getDate(),
            income.getCategory()
        );
    }
}