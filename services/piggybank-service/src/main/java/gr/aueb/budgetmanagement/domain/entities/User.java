package gr.aueb.budgetmanagement.domain.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;

    @ManyToMany(mappedBy = "members", cascade = CascadeType.ALL)
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonalPiggyBank> piggyBanks = new HashSet<>();

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Invitation> invitations = new HashSet<>();

    protected User() {

    }

    public static User create(Long id) {
        User user = new User();
        user.id = id;
        return user;
    }

    public Long getId() {
        return id;
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public Set<PersonalPiggyBank> getPiggyBanks() {
        return Collections.unmodifiableSet(piggyBanks);
    }

    void addPiggyBank(PersonalPiggyBank piggyBank) {
        if (piggyBank == null) {
            throw new InvalidDomainArgumentException("PiggyBank cannot be null");
        }
        piggyBanks.add(piggyBank);
    }

    public Set<Invitation> getInvitations() {
        return Collections.unmodifiableSet(invitations);
    }

    void addInvitation(Invitation invitation) {
        if (invitation == null) {
            throw new InvalidDomainArgumentException("Invitation cannot be null");
        }
        invitations.add(invitation);
    }

    public boolean containsInvitation(Invitation invitation) {
        return invitations.contains(invitation);
    }

    boolean hasAlreadyBeenInvitedTo(Group group) {
        return invitations.stream()
            .anyMatch(invitation -> invitation.getGroup().equals(group));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        User user = (User) other;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
