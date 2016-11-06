package se.teknikhogskolan.springcasemanagement.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class WorkItem extends AbstractEntity {

    @Column(nullable = false, unique = true)
    private String description;

    private Status status = Status.UNSTARTED;

    @OneToOne(cascade = CascadeType.ALL)
    private Issue issue;

    @ManyToOne
    private User user;

    public enum Status {
        UNSTARTED, STARTED, DONE
    }

    public WorkItem(String description) {
        this.description = description;
    }

    protected WorkItem() {
    }

    public String getDescription() {
        return description;
    }

    public WorkItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public WorkItem setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Issue getIssue() {
        return issue;
    }

    public WorkItem setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    public User getUser() {
        return user;
    }

    public WorkItem setUser(User user) {
        this.user = user;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkItem other = (WorkItem) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public int compareTo(WorkItem other) {
        if (null != getId() && null != other.getId()) {
            if (getId() > other.getId()) return 1;
            if (getId() < other.getId()) return -1;
        }
        int result = getDescription().compareTo(other.getDescription());
        if (result < 0) return -1;
        if (result == 0) return 0;
        return 1;
    }
}