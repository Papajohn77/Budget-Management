package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.dto.AddIncomeDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Income;
import gr.aueb.budgetmanagement.domain.entities.User;
import jakarta.transaction.Transactional;

public class IncomeService {
    private final UserRepository userRepository;

    public IncomeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AddIncomeDTO createIncome(AddIncomeCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Income income = Income.create(
                user,
                command.amount(),
                command.date(),
                command.category()
        );

        userRepository.save(user);

        return new AddIncomeDTO(
                income.getId(),
                income.getAmount(),
                income.getDate(),
                income.getCategory()
        );
    }
}