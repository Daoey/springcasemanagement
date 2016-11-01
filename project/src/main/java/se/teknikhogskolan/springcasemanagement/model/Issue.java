package se.teknikhogskolan.springcasemanagement.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@Entity
public class Issue extends AbstractEntity {

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "issue")
    private WorkItem workItem;
    private String description;
    private boolean active;

    protected Issue() {
    }

    public Issue(String description) {
        this.description = description;
        this.active = true;
    }

    public WorkItem getWorkItem() {
        return workItem;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWorkItem(WorkItem workItem) {
        this.workItem = workItem;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}