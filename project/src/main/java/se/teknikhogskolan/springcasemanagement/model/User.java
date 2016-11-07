package se.teknikhogskolan.springcasemanagement.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class User extends AbstractEntity {

    @Column(unique = true)
    private Long userNumber;

    @Column(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    @ManyToOne
    private Team team;
    
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Collection<WorkItem> workItems;

    private boolean active;

    protected User() {
    }

    public User(Long userNumber, String username, String firstName, String lastName) {
        this.userNumber = userNumber;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.team = null;
        this.active = true;
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(Long userNumber) {
        this.userNumber = userNumber;
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

    public User setTeam(Team team) {
        this.team = team;
        return this;
    }
    
    public Collection<WorkItem> getWorkItems() {
        return workItems;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof User) {
            User otherUser = (User) obj;
            return userNumber.equals(otherUser.getUserNumber()) && username.equals(otherUser.getUsername());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result += result * userNumber.hashCode();
        result += result * username.hashCode();
        return result;
    }

}
