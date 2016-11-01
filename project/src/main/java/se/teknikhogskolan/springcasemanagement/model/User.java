package se.teknikhogskolan.springcasemanagement.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class User {

    private String username;
    private String firstName;
    private String lastName;
    @ManyToOne
    private Team team;
    private boolean active;

    protected User() {
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Team getTeam() {
        return team;
    }
    
    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
