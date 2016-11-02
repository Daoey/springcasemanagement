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
}
