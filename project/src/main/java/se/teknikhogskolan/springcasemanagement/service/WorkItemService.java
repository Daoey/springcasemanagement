package se.teknikhogskolan.springcasemanagement.service;

import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

import java.util.Collection;
import java.util.function.Function;

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
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            Issue issue = workItem.getIssue();
            workItem = workItemRepository.save(workItem.setIssue(null));
            issueRepository.delete(issue.getId());
            return workItem;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Cannot find WorkItem with id: " + workItemId, e);
        } catch (Exception e) {
            throw new ServiceException("Cannot remove Issue from WorkItem. WorkItem id: " + workItemId, e);
        }
    }

    public Collection<WorkItem> getAllWithIssue() {
        Collection<WorkItem> workItems;
        try {
            workItems = workItemRepository.findByIssueIsNotNull();
        } catch (Exception e) {
            throw new ServiceException("Cannot get all WorkItems with Issue", e);
        }
        ifEmptyThrowNoSearchResultException(workItems);
        return workItems;
    }

    private void ifEmptyThrowNoSearchResultException(Collection collection) {
        if (null == collection || collection.isEmpty()) {
            throw new NoSearchResultException();
        }
    }

    public WorkItem addIssueToWorkItem(Long issueId, Long workItemId) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            WorkItem workItem = workItemRepository.findOne(workItemId);
            if (DONE.equals(workItem.getStatus())) {
                workItem.setStatus(UNSTARTED);
                workItem.setIssue(issue);
                return workItemRepository.save(workItem);
            } else
                throw new ServiceException(
                        "Issue can only be added to WorkItem with Status DONE, Status was " + workItem.getStatus());
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Cannot find WorkItem with id: " + workItemId, e);
        } catch (Exception e) {
            throw new ServiceException("Cannot add Issue to WorkItem. WorkItem id: " + workItemId, e);
        }
    }

    public Issue createIssue(String description) {
        try {
            return issueRepository.save(new Issue(description));
        } catch (Exception e) {
            throw new ServiceException("Cannot create Issue with description: " + description, e);
        }
    }

    public Collection<WorkItem> getByTeamId(Long teamId) {
        return executeMany(workItemRepository -> {
            return workItemRepository.findByTeamId(teamId);
        }, String.format("Could not get WorkItems by Team id: &s", teamId));
    }

    private Collection<WorkItem> executeMany(Function<WorkItemRepository, Collection<WorkItem>> operation,
            String exceptionMessage) {
        Collection<WorkItem> result;
        try {
            result = operation.apply(workItemRepository);
            if (result.isEmpty()) throw new NoSearchResultException(exceptionMessage);
            return result;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException(exceptionMessage, e);
        } catch (Exception e) {
            throw new ServiceException(exceptionMessage, e);
        }
    }

    public WorkItem create(String description) {
        return executeOne(workItemRepository -> {
            return workItemRepository.save(new WorkItem(description));
        }, String.format("Cannot create WorkItem with description: %s", description));
    }

    private WorkItem executeOne(Function<WorkItemRepository, WorkItem> operation,
            String exceptionMessage) {
        try {
            WorkItem workItem = operation.apply(workItemRepository);
            if (null == workItem) {
                throw new NoSearchResultException(exceptionMessage);
            }
            return workItem;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(exceptionMessage, e);
        }
    }

    public WorkItem setStatus(Long workItemId, WorkItem.Status status) {
        try {
            WorkItem workItem = workItemRepository.findOne(workItemId);
            workItem.setStatus(status);
            return workItemRepository.save(workItem);
        } catch (NullPointerException e) {
            throw new NoSearchResultException(String.format("Cannot set %s on %s", status, workItemId), e);
        } catch (Exception e) {
            throw new ServiceException(String.format("Cannot set %s on %s", status, workItemId), e);
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
        } catch (Exception e) {
            throw new ServiceException(String.format("Cannot get WorkItem with id %d", workItemId), e);
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
        } catch (Exception e) {
            throw new ServiceException(String.format("Cannot remove WorkItem with id %d", workItemId), e);
        }
    }

    public Collection<WorkItem> getByStatus(WorkItem.Status status) {
        return executeMany(workItemRepository -> {
            return workItemRepository.findByStatus(status);
        }, String.format("Cannot get WorkItems by Status %s", status));
    }

    public Collection<WorkItem> getByUserNumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        if (null == user) {
            throw new NoSearchResultException(String.format("Cannot find User with usernNumber %d", userNumber));
        }
        return  executeMany(workItemRepository -> {
            return workItemRepository.findByUserId(user.getId());
        }, String.format("Cannot get WorkItems by userNumber %d", userNumber));
    }

    public Collection<WorkItem> getByDescriptionContains(String text) {
        return  executeMany(workItemRepository -> {
            return workItemRepository.findByDescriptionContains(text);
        }, String.format("Cannot get WorkItems by description contains %d", text));
    }

    public WorkItem setUser(Long userNumber, Long workItemId) {
        User user = getUserByUsernumber(userNumber);
        if (userCanHaveOneMoreWorkItem(user)) {
            WorkItem workItem = getWorkItemById(workItemId);
            workItem.setUser(user);
            return save(workItem);
        } else
            throw new ServiceException("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
    }
    
    private WorkItem save(WorkItem workItem) {
        return executeOne(workItemRepository -> {
            return workItemRepository.save(workItem);
        }, String.format("Cannot save WorkItem %d", workItem.getId()));
    }

    private WorkItem getWorkItemById(Long workItemId) {
        return executeOne(workItemRepository -> {
            return workItemRepository.findOne(workItemId);
        }, String.format("Cannot find WorkItem %d", workItemId));
    }

    private User getUserByUsernumber(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        if (null == user) {
            throw new NoSearchResultException(String.format("Cannot find User %d", userNumber));
        }
        return user;
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
