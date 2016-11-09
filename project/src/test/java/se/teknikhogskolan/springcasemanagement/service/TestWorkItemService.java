package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

public final class TestWorkItemService {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private WorkItem workItem;

    @Mock
    private User user;

    @Mock
    private Issue issue;

    @Mock
    private WorkItemRepository workItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private WorkItemService workItemService;

    private final Long workItemId = 235235L;
    private final Long userNumber = 23553L;
    private final Long userId = 589L;
    private final Long teamId = 23353265L;
    private final Long issueId = 23523L;
    private Collection<WorkItem> workItems = new ArrayList<>();
    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        workItems.clear();
    }

    @Test
    public void canGetById() {
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        WorkItem result = workItemService.getById(workItemId);
        assertEquals(workItem, result);
    }

    @Test
    public void settingInactiveUserToWorkItemShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(false);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void settingUserWithFiveWorkItemToSixthWorkItemShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage("Cannot set User to WorkItem. User is inactive or have 5 WorkItems");
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItems = createListWithWorkItems(5);
        when(workItemRepository.findByUserId(userId)).thenReturn(workItems);
        workItemService.setUser(userNumber, workItemId);
    }

    private Collection<WorkItem> createListWithWorkItems(int amountOfItems) {
        Collection<WorkItem> items = new ArrayList<>();
        for (int i = 0; i < amountOfItems; i++)
            items.add(new WorkItem("WorkItem #" + i));
        return items;
    }

    @Test
    public void canSetUserToWorkItem() {
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(true);
        when(user.getUserNumber()).thenReturn(userNumber);
        when(workItem.getId()).thenReturn(workItemId);
        workItems = createListWithWorkItems(4);
        when(user.getId()).thenReturn(userId);
        when(workItemRepository.findByUserId(userId)).thenReturn(workItems);
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItemService.setUser(userNumber, workItemId);
        verify(workItem).setUser(user);
        verify(workItemRepository).save(workItem);
    }

    @Test
    public void canGetWorkItemsByUserId() {
        WorkItem workItem = new WorkItem("Get by User");
        workItem.setUser(user);
        Collection<WorkItem> workItemsWithOurUser = new ArrayList<>();
        workItemsWithOurUser.add(workItem);

        when(user.getId()).thenReturn(userId);
        when(user.getUserNumber()).thenReturn(userNumber);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(workItemRepository.findByUserId(userId)).thenReturn(workItemsWithOurUser);

        Collection<WorkItem> result = workItemService.getByUserNumber(userNumber);

        verify(workItemRepository).findByUserId(userId);
        assertEquals(workItemsWithOurUser, result);
        result.forEach(item -> {
            assertEquals(userId, item.getUser().getId());
        });
    }

    @Test
    public void canRemoveIssue() {
        Long issueId = 32532L;
        when(issue.getId()).thenReturn(issueId);
        when(workItem.getId()).thenReturn(workItemId);
        when(workItem.getIssue()).thenReturn(issue);
        when(workItem.setIssue(null)).thenReturn(workItem);
        when(workItemRepository.save(workItem)).thenReturn(workItem);
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);

        WorkItem result = workItemService.removeIssueFromWorkItem(workItemId);

        assertEquals(workItem, result);
        verify(workItem).setIssue(null);
        verify(workItemRepository).save(workItem);
        verify(issueRepository).delete(issue.getId());
    }

    @Test
    public void removingIssueFromWorkItemWithoutIssueShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(
                String.format("Cannot remove Issue from WorkItem %d, no Issue found in WorkItem", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        when(workItem.getIssue()).thenReturn(null);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemNotFoundInDatabaseShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot find WorkItem with id '%d'", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot remove Issue from WorkItem. WorkItem id '%d'", workItemId));
        doThrow(dataAccessException).when(workItemRepository).findOne(workItemId);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void canGetAllWithIssue() {
        Collection<WorkItem> workItemsWithIssue = new ArrayList<>();
        workItemsWithIssue.add(workItem);

        when(workItemRepository.findByIssueIsNotNull()).thenReturn(workItemsWithIssue);
        when(workItem.getIssue()).thenReturn(issue);

        workItems = workItemService.getAllWithIssue();

        verify(workItemRepository).findByIssueIsNotNull();
        workItems.forEach(item -> {
            assertNotNull(item.getIssue());
        });
    }

    @Test
    public void canGetAllWithIssueWhenThereIsNoIssuesShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(workItems);
        workItems = workItemService.getAllWithIssue();
    }

    @Test
    public void canGetAllWithIssueReturnsNullShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(null);
        workItems = workItemService.getAllWithIssue();
    }

    @Test
    public void canGetAllWithIssueShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        doThrow(dataAccessException).when(workItemRepository).findByIssueIsNotNull();
        workItems = workItemService.getAllWithIssue();
    }

    @Test
    public void addingIssueToWorkItemWithWrongStatusShouldThrowException() {
        Status wrongStatus = Status.STARTED;
        exception.expect(ServiceException.class);
        exception.expectMessage(
                String.format("Issue can only be added to WorkItem with Status 'DONE', Status was '%s'", wrongStatus));
        when(workItem.getStatus()).thenReturn(wrongStatus);
        when(issueRepository.findOne(issue.getId())).thenReturn(issue);
        when(workItemRepository.findOne(workItem.getId())).thenReturn(workItem);
        workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());
    }

    @Test
    public void addingIssueToWorkItemShouldChangeWorkItemStatus() {
        when(workItem.getStatus()).thenReturn(Status.DONE);
        when(issueRepository.findOne(issue.getId())).thenReturn(issue);
        when(workItemRepository.findOne(workItem.getId())).thenReturn(workItem);

        workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());

        verify(workItem).setStatus(Status.UNSTARTED);
        verify(workItem).setIssue(issue);
        verify(workItemRepository).save(workItem);
    }

    @Test
    public void addingNotFoundIssueToWorkItemWithShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(
                String.format("Cannot find Issue with id '%d'", issueId));
        when(issueRepository.findOne(issueId)).thenReturn(null);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void canPersistIssue() {
        String issueTitle = "This is an issue!";
        workItemService.createIssue(issueTitle);
        verify(issueRepository).save(new Issue(issueTitle));
    }

    @Test
    public void canFindByTeamId() {
        workItems.add(workItem);
        when(workItemRepository.findByTeamId(teamId)).thenReturn((List<WorkItem>) workItems);
        workItemService.getByTeamId(teamId);
        verify(workItemRepository).findByTeamId(teamId);
    }

    @Test
    public void canFindByTeamIdReturnsWithoutResultShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot not get WorkItems by Team id '%s'", teamId));
        workItemService.getByTeamId(teamId);
        verify(workItemRepository).findByTeamId(teamId);
    }

    @Test
    public void canFindByDescriptionContains() {
        String searchText = "important";
        workItems = new ArrayList<>();
        workItems.add(workItem);
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(workItems);
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByDescriptionContainsWithNoMatchShouldThrowException() {
        String searchText = "important";
        exception.expect(ServiceException.class);
        exception.expectMessage("Cannot get WorkItems by description contains '" + searchText + "'");
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(workItems);
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByDescriptionContainsReturnsNullShouldThrowException() {
        String searchText = "important";
        exception.expect(ServiceException.class);
        exception.expectMessage("Cannot get WorkItems by description contains '" + searchText + "'");
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(null);
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByStatus() {
        Status wantedStatus = Status.STARTED;
        workItems.add(workItem);
        when(workItemRepository.findByStatus(wantedStatus)).thenReturn(workItems);
        workItemService.getByStatus(wantedStatus);
        verify(workItemRepository).findByStatus(wantedStatus);
    }

    @Test
    public void canFindByStatusShouldThrowExceptionIfNoWorkItemFound() {
        Status wantedStatus = Status.STARTED;
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot get WorkItems by Status '%s'", wantedStatus));
        workItemService.getByStatus(wantedStatus);
        verify(workItemRepository).findByStatus(wantedStatus);
    }

    @Test
    public void canRemoveWorkItem() {
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItemService.removeById(workItemId);
        verify(workItemRepository).delete(workItem);
    }

    @Test
    public void canChangeWorkItemStatus() {
        Status newStatus = Status.DONE;
        when(workItemRepository.findOne(workItem.getId())).thenReturn(workItem);
        workItemService.setStatus(workItem.getId(), newStatus);
        verify(workItem).setStatus(newStatus);
        verify(workItemRepository).save(workItem);
    }

    @Test
    public void canCreatePersistedWorkItem() {
        String workItemDescription = "Do something!";
        when(workItemRepository.save(new WorkItem(workItemDescription))).thenReturn(workItem);
        WorkItem result = workItemService.create(workItemDescription);
        verify(workItemRepository).save(new WorkItem(workItemDescription));
        assertEquals(workItem, result);
    }
}
