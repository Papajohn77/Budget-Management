package gr.aueb.budgetmanagement.domain.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_seq")
    @SequenceGenerator(name = "group_seq", sequenceName = "group_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupPiggyBank> piggyBanks = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_groups",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    protected Group() {

    }

    public static Group create(String name, User admin) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainArgumentException("Group name cannot be null or blank");
        }

        if (admin == null) {
            throw new InvalidDomainArgumentException("Group admin cannot be null");
        }

        Group group = new Group();
        group.name = name;
        group.admin = admin;
        group.addMember(admin);
        return group;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getAdmin() {
        return admin;
    }

    public boolean isAdmin(User user) {
        return admin.equals(user);
    }

    public Set<User> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public void addMember(User user) {
        if (user == null) {
            throw new InvalidDomainArgumentException("User cannot be null");
        }
        members.add(user);
    }

    public Set<GroupPiggyBank> getPiggyBanks() {
        return Collections.unmodifiableSet(piggyBanks);
    }

    void addPiggyBank(GroupPiggyBank piggyBank) {
        if (piggyBank == null) {
            throw new InvalidDomainArgumentException("PiggyBank cannot be null");
        }
        piggyBanks.add(piggyBank);
    }
}
