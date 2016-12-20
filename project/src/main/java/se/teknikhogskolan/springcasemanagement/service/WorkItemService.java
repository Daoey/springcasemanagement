package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.*;
import se.teknikhogskolan.springcasemanagement.service.wrapper.Piece;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

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

    public WorkItem create(String description) {
        return saveWorkItem(new WorkItem(description));
    }

    private WorkItem saveWorkItem(WorkItem workItem) {
        try {
            return workItemRepository.save(workItem);
        } catch (DataIntegrityViolationException e) {
            throw new NotAllowedException(String.format(
                    "Cannot save WorkItem. Description '%s' violates data integrity.",
                    workItem.getDescription(), e));
        } catch (DataAccessException e) {
            throw new DatabaseException(
                    String.format("Cannot save WorkItem with description '%s'", workItem.getDescription(), e));
        }
    }

    public WorkItem getById(Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)) throw new NotFoundException(String.format("Cannot find WorkItem with id '%d'.", workItemId));
        return workItem;
    }

    private WorkItem getWorkItemById(Long workItemId) {
        try {
            return workItemRepository.findOne(workItemId);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get WorkItem with id '%d'.", workItemId));
        }
    }

    public List<WorkItem> getCompletedWorkItems(LocalDate from, LocalDate to) {
        String exceptionMessage = "Cannot get completed WorkItems.";
        return executeList(workItemRepository -> workItemRepository.findByCompletionDate(from, to), exceptionMessage);
    }

    public List<WorkItem> getByCreatedBetweenDates(LocalDate fromDate, LocalDate toDate) {
        String exceptionMessage = String.format("Cannot get WorkItems between '%s' and '%s'.", toDate, fromDate);
        return executeList(workItemRepository -> workItemRepository.findByCreationDate(fromDate, toDate), exceptionMessage);
    }

    public Piece<WorkItem> getAllByPiece(int page, int pageSize) {
        return new Piece<>(getAllByPage(new PageRequest(page, pageSize)));
    }

    private Page<WorkItem> getAllByPage(PageRequest pageRequest) {
        try {
            return workItemRepository.findAll(pageRequest);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get Page '%d' with size '%d'.",
                    pageRequest.getPageNumber(), pageRequest.getPageSize()), e);
        }
    }

    public Issue createIssue(String description) {
        try {
            return issueRepository.save(new Issue(description));
        } catch (DataIntegrityViolationException e) {
            throw new NotAllowedException(String.format("Issue with description '%s' violates data integrity.", description, e));
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot create Issue with description '%s'.", description), e);
        }
    }

    public WorkItem addIssueToWorkItem(Long issueId, Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)) throw new NotFoundException(String.format("No WorkItem with id '%d' exists.", workItemId))
                .setMissingEntity(WorkItem.class);
        Issue issue = getIssueById(issueId);
        if (nullOrEmpty(issue)) throw new NotFoundException(String.format("No Issue with id '%d' exists.", issueId))
                .setMissingEntity(Issue.class);
        return add(issue, workItem);
    }

    private Issue getIssueById(Long issueId) {
        try {
            return issueRepository.findOne(issueId);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get Issue with id '%d'.", issueId), e);
        }
    }

    private WorkItem add(Issue issue, WorkItem workItem) {
        if (!DONE.equals(workItem.getStatus())){
            throw new NotAllowedException(String.format(
                    "Issue can only be added to WorkItem with Status 'DONE', Status was '%s'. Issue id '%d', WorkItem id '%d'.",
                    workItem.getStatus(), issue.getId(), workItem.getId()));
        }

        workItem.setStatus(UNSTARTED);
        workItem.setIssue(issue);
        return saveWorkItem(workItem);
    }

    @Transactional
    public WorkItem removeIssueFromWorkItem(Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)) throw new NotFoundException(String.format("No WorkItem with id '%d'.", workItemId))
                .setMissingEntity(WorkItem.class);
        return removeIssueFrom(workItem);
    }

    private WorkItem removeIssueFrom(WorkItem workItem) {
        Issue issue = workItem.getIssue();
        if (nullOrEmpty(issue)) throw new NotFoundException(
                String.format("Cannot remove Issue from WorkItem with id '%d', no Issue in WorkItem.", workItem.getId()))
                .setMissingEntity(Issue.class);

        WorkItem backupToReturnDeletedData = workItem;
        saveWorkItem(workItem.setIssue(null));
        deleteIssue(issue);
        return backupToReturnDeletedData;
    }

    private Issue deleteIssue(Issue issue) {
        try {
            issueRepository.delete(issue.getId());
            return issue;
        } catch (DataAccessException e) {
            throw new DatabaseException(
                    String.format("Cannot remove Issue from WorkItem. Issue id '%d'.", issue.getId()), e);
        }
    }

    public Collection<WorkItem> getAllWithIssue() {
        String exceptionMessage = "Cannot get all WorkItems with Issue.";
        return executeCollection(workItemRepository -> workItemRepository.findByIssueIsNotNull(), exceptionMessage);
    }

    private Collection<WorkItem> executeCollection(Function<WorkItemRepository, Collection<WorkItem>> operation,
            String exceptionMessage) {
        try {
            return operation.apply(workItemRepository);
        } catch (DataAccessException e) {
            throw new DatabaseException(exceptionMessage, e);
        }
    }

    public Collection<WorkItem> getByTeamId(Long teamId) {
        String exceptionMessage = String.format("Cannot not get WorkItems by Team id '%s'.", teamId);
        return executeCollection(workItemRepository -> workItemRepository.findByTeamId(teamId), exceptionMessage);
    }

    public WorkItem setStatus(Long workItemId, WorkItem.Status status) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)){
            throw new NotFoundException(String.format("No WorkItem with id '%d'.", workItemId));
        }

        workItem.setStatus(status);
        if (status.equals(DONE)) {
            workItem.setCompletionDate(LocalDate.now());
        }
        return saveWorkItem(workItem);
    }

    private List<WorkItem> executeList(Function<WorkItemRepository, List<WorkItem>> operation,
            String exceptionMessage) {
        try {
            return operation.apply(workItemRepository);
        } catch (DataAccessException e) {
            throw new DatabaseException(exceptionMessage, e);
        }
    }

    public WorkItem removeById(Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)) throw new NotFoundException(String.format("No WorkItem with id '%d'", workItemId));
        return delete(workItem);
    }

    private WorkItem delete(WorkItem workItem) {
        try {
            workItemRepository.delete(workItem);
            return workItem;
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot remove WorkItem with id '%d'", workItem.getId()), e);
        }
    }

    public Collection<WorkItem> getByStatus(WorkItem.Status status) {
        String exceptionMessage = String.format("Cannot get WorkItems by Status '%s'", status);
        return executeCollection(workItemRepository -> workItemRepository.findByStatus(status), exceptionMessage);
    }

    public Collection<WorkItem> getByUsernumber(Long userNumber) {
        User user = getUserByUsernumber(userNumber);
        if (nullOrEmpty(user)){
            throw new NotFoundException(String.format("No User with Usernumber '%d'.", userNumber)).setMissingEntity(User.class);
        }
        String exceptionMessage = String.format("Cannot get WorkItems by userNumber '%d'", userNumber);
        return executeCollection(workItemRepository -> workItemRepository.findByUserId(user.getId()), exceptionMessage);
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        String exceptionMessage = String.format("Cannot get WorkItems by description contains '%s'", text);
        return executeCollection(workItemRepository -> workItemRepository.findByDescriptionContains(text), exceptionMessage);
    }

    private boolean nullOrEmpty(Object object) {
        if (null == object) {
            return true;
        }
        if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            if (collection.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public WorkItem setUser(Long userNumber, Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (nullOrEmpty(workItem)) throw new NotFoundException(String.format("No WorkItem with id '%d'", workItemId))
                .setMissingEntity(WorkItem.class);

        User user = getUserByUsernumber(userNumber);
        if (nullOrEmpty(user)) throw new NotFoundException(String.format("No User with usernumber '%d'.", userNumber))
                .setMissingEntity(User.class);
        if (notActive(user)) throw new NotAllowedException(String.format("User with usernumber '%d' is inactive. Only active User can be assigned to WorkItem", userNumber));
        if (userReachedWorkItemLimit(user)) throw new MaximumQuantityException("User already have maximum amount of WorkItems");

        workItem.setUser(user);
        return saveWorkItem(workItem);
    }

    private User getUserByUsernumber(Long userNumber) {
        try {
            return userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get User by userNumber '%d'", userNumber), e);
        }
    }

    private boolean notActive(User user) {
        return !user.isActive();
    }

    private boolean userReachedWorkItemLimit(User user) {
        Collection<WorkItem> workItemsToThisUser = workItemRepository.findByUserId(user.getId());
        final int maxAllowedWorkItemsPerUser = 5;
        if (null == workItemsToThisUser || workItemsToThisUser.size() < maxAllowedWorkItemsPerUser){
            return false;
        }
        return true;
    }
}