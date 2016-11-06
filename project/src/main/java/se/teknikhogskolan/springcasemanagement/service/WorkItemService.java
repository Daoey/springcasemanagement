package se.teknikhogskolan.springcasemanagement.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final IssueRepository issueRepository;

    @Autowired
    public WorkItemService(WorkItemRepository workItemRepository, UserRepository userRepository,
            TeamRepository teamRepository, IssueRepository issueRepository) {
        this.workItemRepository = workItemRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.issueRepository = issueRepository;
    }
    
    public WorkItem removeIssueFromWorkItem(WorkItem workItem) {
        Issue issue = workItem.getIssue();
        workItem = workItemRepository.save(workItem.setIssue(null));
        issueRepository.delete(issue.getId());
        return workItem;
    }
    
    public Collection<WorkItem> getAllWithIssue() {
        return workItemRepository.findByIssueIsNotNull();
    }

    public WorkItem addIssueToWorkItem(Issue issue, WorkItem workItem) {
        if (workItem.getStatus() == Status.DONE) {
            workItem.setStatus(Status.UNSTARTED);
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

    public WorkItem createWorkItem(String description) {
        return workItemRepository.save(new WorkItem(description));
    }

    public WorkItem setWorkItemStatus(WorkItem workItem, WorkItem.Status status) {
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

    public Collection<WorkItem> getByUserId(Long userId) {
        return workItemRepository.findByUserId(userId);
    }

    public Collection<WorkItem> getByUserNumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        return getByUserId(user.getId());
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        return workItemRepository.findByDescriptionContains(text);
    }

    public WorkItem setUserToWorkItem(Long userNumber, WorkItem workItem) {
        User user = userRepository.findByUserNumber(userNumber);
        if (userCanHaveOneMoreWorkItem(user)) {
            workItem.setUser(user);
            return workItemRepository.save(workItem);
        } else
            throw new ServiceException("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
    }

    private boolean userCanHaveOneMoreWorkItem(User user) {
        if (user.isActive() & getByUserId(user.getId()).size() < 5) {
            return true;
        }
        return false;
    }
}
