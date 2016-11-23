package se.teknikhogskolan.springcasemanagement.service;

import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.ForbiddenOperationException;
import se.teknikhogskolan.springcasemanagement.service.exception.InvalidInputException;
import se.teknikhogskolan.springcasemanagement.service.exception.MaximumQuantityException;
import se.teknikhogskolan.springcasemanagement.service.exception.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.wrapper.Piece;

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

    public WorkItem getById(Long workItemId) {
        WorkItem result = getWorkItemById(workItemId);
        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(String.format("No match for WorkItem with id '%d'", workItemId));
    }

    private WorkItem getWorkItemById(Long workItemId) {
        try {
            return workItemRepository.findOne(workItemId);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get WorkItem '%d'", workItemId));
        }
    }

    public List<WorkItem> getByCreatedBetweenDates(LocalDate fromDate, LocalDate toDate) {
        List<WorkItem> result = executeList(workItemRepository -> {
            return workItemRepository.findByCreationDate(fromDate, toDate);
        }, String.format("Cannot get WorkItems between '%s' and '%s'", toDate, fromDate));

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(
                    String.format("No WorkItems found between dates '%s' and '%s'", fromDate, toDate));
    }

    public Piece<WorkItem> getAllByPage(int page, int pageSize) {
        Piece<WorkItem> result = new Piece<>(getAllByPage(new PageRequest(page, pageSize)));
        throwNoSearchResultExceptionIfResultIsEmpty(result,
                String.format("No WorkItems found when requesting page #%d and page size '%d'", page, pageSize));
        return result;
    }

    private Page<WorkItem> getAllByPage(PageRequest pageRequest) {
        try {
            return workItemRepository.findAll(pageRequest);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get Page '%d' with size '%d'",
                    pageRequest.getPageNumber(), pageRequest.getPageSize()), e);
        }
    }

    private void throwNoSearchResultExceptionIfResultIsEmpty(Piece<WorkItem> result, String exceptionMessage) {
        if (null == result || !result.hasContent()) {
            throw new NoSearchResultException(exceptionMessage);
        }
    }

    @Transactional // TODO test @Transactional
    public WorkItem removeIssueFromWorkItem(Long workItemId) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (weHaveA(workItem)) {
            return removeIssueFrom(workItem);
        } else
            throw new NoSearchResultException(String.format("No match for WorkItem with id '%d'", workItemId));
    }

    private WorkItem removeIssueFrom(WorkItem workItem) {
        Issue issue = workItem.getIssue();
        if (weHaveA(issue)) {
            saveWorkItem(workItem.setIssue(null));
            deleteIssue(issue);
            return workItem;
        } else
            throw new ForbiddenOperationException(String
                    .format("Cannot remove Issue from WorkItem %d, no Issue found in WorkItem", workItem.getId()));
    }

    private WorkItem saveWorkItem(WorkItem workItem) {
        try {
            return workItemRepository.save(workItem);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException(String.format("WorkItem with description '%s' violates data integrity",
                    workItem.getDescription(), e));
        } catch (DataAccessException e) {
            throw new DatabaseException(
                    String.format("Cannot save WorkItem with description '%s'", workItem.getDescription(), e));
        }
    }

    private Issue deleteIssue(Issue issue) {
        try {
            issueRepository.delete(issue.getId());
            return issue;
        } catch (DataAccessException e) {
            throw new DatabaseException(
                    String.format("Cannot remove Issue from WorkItem. Issue id '%d'", issue.getId()), e);
        }
    }

    public Collection<WorkItem> getAllWithIssue() {
        Collection<WorkItem> result = executeCollection(workItemRepository -> {
            return workItemRepository.findByIssueIsNotNull();
        }, "Cannot get all WorkItems with Issue");
        if (null == result || result.isEmpty()) {
            throw new NoSearchResultException("No match for get all WorkItems with Issue");
        } else
            return result;
    }

    private Collection<WorkItem> executeCollection(Function<WorkItemRepository, Collection<WorkItem>> operation,
            String exceptionMessage) {
        try {
            return operation.apply(workItemRepository);
        } catch (DataAccessException e) {
            throw new DatabaseException(exceptionMessage, e);
        }
    }

    public WorkItem addIssueToWorkItem(Long issueId, Long workItemId) {
        Issue issue = getIssueById(issueId);
        WorkItem workItem = getWorkItemById(workItemId);
        if (weHaveA(workItem)) {
            return add(issue, workItem);
        } else
            throw new NoSearchResultException(String.format("No match for WorkItem with id '%d'", workItemId));
    }

    private WorkItem add(Issue issue, WorkItem workItem) {
        if (DONE.equals(workItem.getStatus())) {
            workItem.setStatus(UNSTARTED);
            workItem.setIssue(issue);
            return saveWorkItem(workItem);
        } else throw new InvalidInputException(String.format(
                "Issue can only be added to WorkItem with Status 'DONE', Status was '%s'. Issue id '%d', WorkItem id '%d'",
                    workItem.getStatus(), issue.getId(), workItem.getId()));
    }

    private Issue getIssueById(Long issueId) {
        Issue issue;
        try {
            issue = issueRepository.findOne(issueId);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get Issue with id '%d'", issueId), e);
        }
        if (weHaveA(issue))
            return issue;
        else
            throw new NoSearchResultException(String.format("No match for Issue with id '%d'", issueId));
    }

    public Issue createIssue(String description) {
        try {
            return issueRepository.save(new Issue(description));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException(String.format("Issue with description '%s' violates data integrity",
                    description, e));
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot create Issue with description '%s'", description), e);
        }
    }

    public Collection<WorkItem> getByTeamId(Long teamId) {
        Collection<WorkItem> result = executeCollection(workItemRepository -> {
            return workItemRepository.findByTeamId(teamId);
        }, String.format("Cannot not get WorkItems by Team id '%s'", teamId));

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(String.format("No match for WorkItems with team id '%d'", teamId));
    }

    public WorkItem setStatus(Long workItemId, WorkItem.Status status) {
        WorkItem workItem = getWorkItemById(workItemId);
        if (weHaveA(workItem)) {
            workItem.setStatus(status);
            if (status.equals(DONE)) {
                workItem.setCompletionDate(LocalDate.now());
            }
            return saveWorkItem(workItem);
        } else
            throw new NoSearchResultException(String.format("No match for WorkItem with id '%d'", workItemId));
    }

    public WorkItem create(String description) {
        return saveWorkItem(new WorkItem(description));
    }

    public List<WorkItem> getCompletedWorkItems(LocalDate from, LocalDate to) {
        List<WorkItem> result = executeList(workItemRepository -> {
            return workItemRepository.findByCompletionDate(from, to);
        }, "Cannot get completed WorkItems");

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(
                    String.format("No match for WorkItems completed between '%s' and '%s'", to, from));
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
        if (weHaveA(workItem)) {
            return delete(workItem);
        } else
            throw new NoSearchResultException(String.format("No match for WorkItem with id '%d'", workItemId));
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
        Collection<WorkItem> result = executeCollection(workItemRepository -> {
            return workItemRepository.findByStatus(status);
        }, String.format("Cannot get WorkItems by Status '%s'", status));

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(String.format("No match for get WorkItems by Status '%s'", status));
    }

    public Collection<WorkItem> getByUsernumber(Long userNumber) {
        User user = getUserByUsernumber(userNumber);
        Collection<WorkItem> result = executeCollection(workItemRepository -> {
            return workItemRepository.findByUserId(user.getId());
        }, String.format("Cannot get WorkItems by userNumber '%d'", userNumber));

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(
                    String.format("No match for WorkItems added to User with Usernumber '%d'", userNumber));
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        Collection<WorkItem> result = executeCollection(workItemRepository -> {
            return workItemRepository.findByDescriptionContains(text);
        }, String.format("Cannot get WorkItems by description contains '%s'", text));

        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(String.format("No match for WorkItem description contains '%s'", text));
    }

    private boolean weHaveA(Object result) {
        if (null == result) {
            return false;
        }
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            if (collection.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public WorkItem setUser(Long userNumber, Long workItemId) {
        User user = getUserByUsernumber(userNumber);
        if (user.isActive()) {
            if (userHasRoomForOneMoreWorkItem(user)) {
                WorkItem workItem = getWorkItemById(workItemId);
                workItem.setUser(user);
                return saveWorkItem(workItem);
            } else
                throw new MaximumQuantityException("User already have maximum amount of WorkItems");
        } else
            throw new InvalidInputException("User is inactive. Only active User can be assigned to WorkItem");
    }

    private User getUserByUsernumber(Long userNumber) {
        User result;
        try {
            result = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Cannot get User by userNumber '%d'", userNumber), e);
        }
        if (weHaveA(result))
            return result;
        else
            throw new NoSearchResultException(String.format("No match for User '%d'", userNumber));
    }

    private boolean userHasRoomForOneMoreWorkItem(User user) {
        Collection<WorkItem> workItemsToThisUser = workItemRepository.findByUserId(user.getId());
        final int maxAllowedWorkItemsPerUser = 5;
        if (null == workItemsToThisUser || workItemsToThisUser.size() < maxAllowedWorkItemsPerUser)
            return true;
        return false;
    }
}