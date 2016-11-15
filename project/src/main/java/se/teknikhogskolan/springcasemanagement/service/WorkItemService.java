package se.teknikhogskolan.springcasemanagement.service;

import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.teknikhogskolan.springcasemanagement.model.AbstractEntity;
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

    public List<WorkItem> getByCreatedBetweenDates(LocalDate fromDate, LocalDate toDate) {
        List<WorkItem> result = getAllCreatedBetweenDates(fromDate, toDate);
        throwNoSearchResultExceptionIfResultIsEmpty(result, 
                String.format("No WorkItems found between dates '%s' and '%s'", fromDate, toDate));
        return result;
    }

    private List<WorkItem> getAllCreatedBetweenDates(LocalDate fromDate, LocalDate toDate) {
        try {
            return workItemRepository.findByCreationDate(fromDate, toDate);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot get WorkItems between '%s' and '%s'", toDate, fromDate),
                    e);
        }
    }

    private void throwNoSearchResultExceptionIfResultIsEmpty(Collection<WorkItem> result, String exceptionMessage) {
        if (null == result || result.isEmpty()) {
            throw new NoSearchResultException(exceptionMessage);
        }
    }

    public Page<WorkItem> getAllByPage(int page, int pageSize) {
        Page<WorkItem> result = getAllByPage(new PageRequest(page, pageSize));
        throwNoSearchResultExceptionIfResultIsEmpty(result,
                String.format("No WorkItems found when requesting page #%d and page size '%d'", page, pageSize));
        return result;
    }

    private Page<WorkItem> getAllByPage(PageRequest pageRequest) {
        try {
            return workItemRepository.findAll(pageRequest);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot get Page '%d' with size '%d'",
                    pageRequest.getPageNumber(), pageRequest.getPageSize()), e);
        }
    }

    private void throwNoSearchResultExceptionIfResultIsEmpty(Page<WorkItem> result, String exceptionMessage) {
        if (null == result || !result.hasContent()) {
            throw new NoSearchResultException(exceptionMessage);
        }
    }
    
    @Transactional // TODO test @Transactional
    public WorkItem removeIssueFromWorkItem(Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        Issue issue = workItem.getIssue();
        throwNoSearchResultExceptionIfNull(issue, String.format("Cannot remove Issue from WorkItem %d, no Issue found in WorkItem", workItemId));
        workItem = saveWorkItem(workItem.setIssue(null));
        issue = deleteIssue(issue);
        return workItem;
    }

    private WorkItem getWorkItemById(Long workItemId) {
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            throwNoSearchResultExceptionIfNull(workItem, String.format("No match for WorkItem with id '%d'", workItemId));
            return workItem;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot find WorkItem '%d'", workItemId));
        }
    }

    private WorkItem saveWorkItem(WorkItem workItem) {
        try {
            return workItemRepository.save(workItem);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot save WorkItem with description '%s'", workItem.getDescription(), e));
        }
    }

    private Issue deleteIssue(Issue issue) {
        try {
            issueRepository.delete(issue.getId());
            return issue;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot remove Issue from WorkItem. Issue id '%d'", issue.getId()), e);
        }
    }

    public Collection<WorkItem> getAllWithIssue() {
        Collection<WorkItem> workItems = executeMany(workItemRepository -> {
            return workItemRepository.findByIssueIsNotNull();
        }, "Cannot get all WorkItems with Issue");
        ifEmptyThrowNoSearchResultException(workItems, "No match for get all WorkItems with Issue");
        return workItems;
    }

    private Collection<WorkItem> executeMany(Function<WorkItemRepository, Collection<WorkItem>> operation,
            String exceptionMessage) {
        try {
            return operation.apply(workItemRepository);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(exceptionMessage, e);
        }
    }
    
    private void ifEmptyThrowNoSearchResultException(Collection<WorkItem> collection, String exceptionMessage) {
        if (null == collection || collection.isEmpty()) {
            throw new NoSearchResultException(exceptionMessage);
        }
    }

    public WorkItem addIssueToWorkItem(Long issueId, Long workItemId) {
        Issue issue = getIssueById(issueId);
        WorkItem workItem = getWorkItemById(workItemId);
        if (DONE.equals(workItem.getStatus())) {
            workItem.setStatus(UNSTARTED);
            workItem.setIssue(issue);
            return saveWorkItem(workItem);
        } else throw new DatabaseException(String.format("Issue can only be added to WorkItem with Status 'DONE', Status was '%s'",
                            workItem.getStatus()));
    }

    private Issue getIssueById(Long issueId) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            throwNoSearchResultExceptionIfNull(issue, String.format("No match for Issue with id '%d'", issueId));
            return issue;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot get Issue with id '%d'", issueId), e);
        }
    }

    public Issue createIssue(String description) {
        try {
            return issueRepository.save(new Issue(description));
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot create Issue with description '%s'", description), e);
        }
    }

    public Collection<WorkItem> getByTeamId(Long teamId) {
        Collection<WorkItem> workItems = executeMany(workItemRepository -> {
            return workItemRepository.findByTeamId(teamId);
        }, String.format("Cannot not get WorkItems by Team id '%s'", teamId));
        throwNoSearchResultExceptionIfResultIsEmpty(workItems, String.format("No match for WorkItems with team id '%d'", teamId));
        return workItems;
    }

    public WorkItem setStatus(Long workItemId, WorkItem.Status status) {
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            workItem.setStatus(status);
            if (status.equals(DONE))
                workItem.setCompletionDate(LocalDate.now());
            return workItemRepository.save(workItem);
        } catch (NullPointerException e) {
            throw new NoSearchResultException(
                    String.format("Cannot set Status '%s' on WorkItem '%s'", status, workItemId), e);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot set Status '%s' on WorkItem '%s'", status, workItemId), e);
        }
    }

    public WorkItem create(String description) {
        try {
            return saveWorkItem(new WorkItem(description));
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot create WorkItem with description '%s'", description), e);
        }
    }

    public List<WorkItem> getCompletedWorkItems(LocalDate from, LocalDate to) {
        try {
            List<WorkItem> workItems = workItemRepository.findByCompletionDate(from, to);
            if (null == workItems) {
                throw new NoSearchResultException(
                        "Could not find any completed work items between dates " + from + " and " + to);
            }
            return workItems;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException("Failed to get completed work items", e);
        }
    }

    public WorkItem getById(Long workItemId) {
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            if (null == workItem) {
                throw new NoSearchResultException(String.format("Cannot find WorkItem with id %d", workItemId));
            }
            return workItem;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot get WorkItem with id %d", workItemId), e);
        }
    }

    public WorkItem removeById(Long workItemId) {
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            if (null == workItem) {
                throw new NoSearchResultException(String.format("Cannot find WorkItem with id %d", workItemId));
            }
            workItemRepository.delete(workItem);
            return workItem;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot remove WorkItem with id '%d'", workItemId), e);
        }
    }

    public Collection<WorkItem> getByStatus(WorkItem.Status status) {
        Collection<WorkItem> workItems = executeMany(workItemRepository -> {
            return workItemRepository.findByStatus(status);
        }, String.format("Cannot get WorkItems by Status '%s'", status));
        throwNoSearchResultExceptionIfResultIsEmpty(workItems, String.format("No match for get WorkItems by Status '%s'", status));
        return workItems;
    }

    public Collection<WorkItem> getByUserNumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        if (null == user) {
            throw new NoSearchResultException(String.format("Cannot find User with usernNumber '%d'", userNumber));
        }
        return executeMany(workItemRepository -> {
            return workItemRepository.findByUserId(user.getId());
        }, String.format("Cannot get WorkItems by userNumber '%d'", userNumber));
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        Collection<WorkItem> workItems = executeMany(workItemRepository -> {
            return workItemRepository.findByDescriptionContains(text);
        }, String.format("Cannot get WorkItems by description contains '%s'", text));
        throwNoSearchResultExceptionIfResultIsEmpty(workItems, String.format("No match for WorkItem description contains '%s'", text));
        return workItems;
    }

    public WorkItem setUser(Long userNumber, Long workItemId) {
        User user = getUserByUsernumber(userNumber);
        if (userCanHaveOneMoreWorkItem(user)) {
            WorkItem workItem = getWorkItemById(workItemId);
            workItem.setUser(user);
            return saveWorkItem(workItem);
        } else
            throw new DatabaseException("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
    }

    private User getUserByUsernumber(Long userNumber) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (NestedRuntimeException e) {
            throw new DatabaseException(String.format("Cannot get User by userNumber '%d'", userNumber), e);
        }
        throwNoSearchResultExceptionIfNull(user, String.format("Cannot find User '%d'", userNumber));
        return user;
    }

    private void throwNoSearchResultExceptionIfNull(AbstractEntity entity, String exceptionMessage) {
        if (null == entity) {
            throw new NoSearchResultException(exceptionMessage);
        }
    }

    private boolean userCanHaveOneMoreWorkItem(User user) {
        if (user.isActive() && userHasRoomForOneMoreWorkItem(user)) {
            return true;
        }
        return false;
    }

    private boolean userHasRoomForOneMoreWorkItem(User user) {
        Collection<WorkItem> workItemsToThisUser = workItemRepository.findByUserId(user.getId());
        final int maxAllowedWorkItemsPerUser = 5;
        if (workItemsToThisUser.size() < maxAllowedWorkItemsPerUser)
            return true;
        return false;
    }
}