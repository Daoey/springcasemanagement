package se.teknikhogskolan.springcasemanagement.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

@Service
public class WorkItemService {

    private final WorkItemRepository repository;
    private final UserRepository userRepository;

    @Autowired
    public WorkItemService(WorkItemRepository workItemRepository, UserRepository userRepository) {
        this.repository = workItemRepository;
        this.userRepository = userRepository;
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

    public WorkItem removeById(Long workItemId) {
        WorkItem workItem = repository.findOne(workItemId);
        repository.delete(workItem);
        return workItem;
    }

    public Collection<WorkItem> getByStatus(WorkItem.Status status) {
        return repository.findByStatus(status);
    }

    public Collection<WorkItem> getByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public Collection<WorkItem> getByUserNumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        return getByUserId(user.getId());
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        return repository.findByDescriptionContains(text);
    }

    public WorkItem setUserToWorkItem(Long userNumber, WorkItem workItem) {
        User user = userRepository.findByUserNumber(userNumber);
        workItem.setUser(user);
        return repository.save(workItem);
    }
}
