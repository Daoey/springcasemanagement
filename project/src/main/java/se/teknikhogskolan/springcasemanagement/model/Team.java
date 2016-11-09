package se.teknikhogskolan.springcasemanagement.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class Team extends AbstractEntity {

    @Column(unique = true)
    private String name;
    private boolean active;

    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    private Collection<User> users;

    protected Team() {
    }

    public Team(String name) {
        this.name = name;
        this.active = true;
        users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public boolean isActive() {
        return active;
    }

    public Team setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Team setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Team) {
            Team otherTeam = (Team) other;
            return name.equals(otherTeam.getName());
        }
        return false;
    }
}
