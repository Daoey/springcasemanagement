package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;

    @Autowired
    public WorkItemService(WorkItemRepository workItemRepository) {
        this.workItemRepository = workItemRepository;
    }
}
