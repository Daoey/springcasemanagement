package se.teknikhogskolan.springcasemanagement.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class Team extends AbstractEntity {

    @Column(unique = true)
    private String name;
    private boolean active;

    @OneToMany(mappedBy = "team")
    private Collection<User> users;

    protected Team() {
    }

    public Team(String name) {
        this.name = name;
        this.active = true;
    }

    public String getName() {
        return name;
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
}
