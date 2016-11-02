package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

@Service
public class WorkItemService {

    private final WorkItemRepository repository;

    @Autowired
    public WorkItemService(WorkItemRepository workItemRepository) {
        this.repository = workItemRepository;
    }

    public WorkItem createWorkItem(String description) {
        return repository.save(new WorkItem(description));
    }

    public WorkItem setWorkItemStatus(WorkItem workItem, WorkItem.Status status) {
        workItem.setStatus(status);
        return repository.save(workItem);
    }
    
    public WorkItem getById(Long id) {
        return repository.findOne(id);
    }
    
    public WorkItem remove(WorkItem workItem) {
        repository.delete(workItem);
        return workItem;
    }
}
