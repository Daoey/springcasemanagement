package se.teknikhogskolan.springcasemanagement.service;

import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;

    @Autowired
    public WorkItemService(WorkItemRepository workItemRepository, UserRepository userRepository,
            IssueRepository issueRepository) {
        this.workItemRepository = workItemRepository;
        this.userRepository = userRepository;
        this.issueRepository = issueRepository;
    }
    
    public WorkItem removeIssueFromWorkItem(Long workItemId) {
        WorkItem workItem = workItemRepository.findOne(workItemId);
        Issue issue = workItem.getIssue();
        workItem = workItemRepository.save(workItem.setIssue(null));
        issueRepository.delete(issue.getId());
        return workItem;
    }
    
    public Collection<WorkItem> getAllWithIssue() {
        return workItemRepository.findByIssueIsNotNull();
    }

    public WorkItem addIssueToWorkItem(Long issueId, Long workItemId) {
        Issue issue = issueRepository.findOne(issueId);
        WorkItem workItem = workItemRepository.findOne(workItemId);
        if (DONE.equals(workItem.getStatus())) {
            workItem.setStatus(UNSTARTED);
            workItem.setIssue(issue);
            return workItemRepository.save(workItem);
        } else
            throw new ServiceException(
                    "Issue can only be added to WorkItem with Status DONE, Status was " + workItem.getStatus());
    }

    public Issue createIssue(String description) {
        return issueRepository.save(new Issue(description));
    }

    public Collection<WorkItem> getByTeamId(Long teamId) {
        return workItemRepository.findByTeamId(teamId);
    }

    public WorkItem create(String description) {
        return workItemRepository.save(new WorkItem(description));
    }

    public WorkItem setStatus(Long workItemId, WorkItem.Status status) {
        WorkItem workItem = workItemRepository.findOne(workItemId);
        workItem.setStatus(status);
        return workItemRepository.save(workItem);
    }

    public WorkItem getById(Long id) {
        return workItemRepository.findOne(id);
    }

    public WorkItem removeById(Long workItemId) {
        WorkItem workItem = workItemRepository.findOne(workItemId);
        workItemRepository.delete(workItem);
        return workItem;
    }

    public Collection<WorkItem> getByStatus(WorkItem.Status status) {
        return workItemRepository.findByStatus(status);
    }

    public Collection<WorkItem> getByUserNumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        return getByUserId(user.getId());
    }

    private Collection<WorkItem> getByUserId(Long userId) {
        return workItemRepository.findByUserId(userId);
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        return workItemRepository.findByDescriptionContains(text);
    }

    public WorkItem setUser(Long userNumber, Long workItemId) {
        User user = userRepository.findByUserNumber(userNumber);
        if (userCanHaveOneMoreWorkItem(user)) {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            workItem.setUser(user);
            return workItemRepository.save(workItem);
        } else
            throw new ServiceException("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
    }

    private boolean userCanHaveOneMoreWorkItem(User user) {
        if (user.isActive() & userHasRoomForOneMoreWorkItem(user)) {
            return true;
        }
        return false;
    }

    private boolean userHasRoomForOneMoreWorkItem(User user) {
        Collection<WorkItem> workItemsToThisUser = getByUserId(user.getId());
        final int maxAllowedWorkItemsPerUser = 5;
        return workItemsToThisUser.size() < maxAllowedWorkItemsPerUser;
    }
}
