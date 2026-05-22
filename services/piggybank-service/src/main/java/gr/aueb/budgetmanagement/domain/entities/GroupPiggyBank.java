package gr.aueb.budgetmanagement.domain.entities;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("GROUP")
public class GroupPiggyBank extends PiggyBank {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    protected GroupPiggyBank() {

    }

    private GroupPiggyBank(String name, Money targetAmount, ExpenseCategory category, Group group) {
        super(name, targetAmount, category);
        this.group = group;
        group.addPiggyBank(this);
    }

    public static GroupPiggyBank create(
        String name, 
        Money targetAmount, 
        ExpenseCategory category, 
        Group group,
        User user
    ) {
        if (group == null) {
            throw new InvalidDomainArgumentException("Group cannot be null");
        }

        if (!group.isAdmin(user)) {
            throw new ForbiddenOperationDomainException("Only group admin can create group piggy banks");
        }

        return new GroupPiggyBank(name, targetAmount, category, group);
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public boolean isAuthorizedUser(User user) {
        return this.group.getMembers().contains(user);
    }

    @Override
    public boolean canBeDissolvedBy(User user) {
        return this.group.getAdmin().equals(user);
    }
}
