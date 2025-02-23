package gr.aueb.budgetmanagement.domain.entities;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("PERSONAL")
public class PersonalPiggyBank extends PiggyBank {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected PersonalPiggyBank() {

    }

    private PersonalPiggyBank(String name, Money targetAmount, ExpenseCategory category, User user) {
        super(name, targetAmount, category);
        this.user = user;
        user.addPiggyBank(this);
    }

    public static PersonalPiggyBank create(String name, Money targetAmount, ExpenseCategory category, User user) {
        if (user == null) {
            throw new InvalidDomainArgumentException("User cannot be null");
        }

        return new PersonalPiggyBank(name, targetAmount, category, user);
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean isAuthorizedUser(User user) {
        return this.user.equals(user);
    }

    @Override
    public boolean canBeDissolvedBy(User user) {
        return this.user.equals(user);
    }
}
