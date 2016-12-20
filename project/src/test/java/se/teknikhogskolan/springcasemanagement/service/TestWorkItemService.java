package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.*;
import se.teknikhogskolan.springcasemanagement.service.wrapper.Piece;

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

    @Mock
    private Piece<WorkItem> piece;

    @Mock
    private Page<WorkItem> page;

    @InjectMocks
    private WorkItemService workItemService;

    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");
    private final Long workItemId = 235235L;
    private final Long userNumber = 23553L;
    private final Long userId = 589L;
    private final Long teamId = 23353265L;
    private final Long issueId = 23523L;
    private Collection<WorkItem> workItemCollection = new ArrayList<>();
    private List<WorkItem> workItemList = new ArrayList<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        workItemCollection.clear();
        workItemList.clear();
    }

    @Test
    public void canGetAllByCreationDate() {
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        workItemList.add(workItem);
        when(workItemRepository.findByCreationDate(fromDate, toDate)).thenReturn(workItemList);
        
        workItemCollection = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        verify(workItemRepository).findByCreationDate(fromDate, toDate);
        assertTrue(workItemCollection.contains(workItem));
    }

    @Test
    public void getAllByCreationDateWithMatchFoundInDatabaseShouldReturnEmptyList() {
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        when(workItemRepository.findByCreationDate(fromDate, toDate)).thenReturn(new ArrayList<>());
        Collection<WorkItem> workItems = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        assertTrue(workItems.isEmpty());
    }

    @Test
    public void canGetAllBySlices() {
        workItemList.add(workItem);
        Page<WorkItem> workItemPage = new PageImpl<>(workItemList);
        PageRequest pageRequest = new PageRequest(0, 10);
        when(workItemRepository.findAll(pageRequest)).thenReturn(workItemPage);
        
        Piece<WorkItem> result = workItemService.getAllByPiece(0, 10);
        assertNotNull(result);
    }

    @Test
    public void canGetAllBySlicesMocked() {
        workItemCollection.add(workItem);
        PageRequest pageRequest = new PageRequest(1, 1);
        when(workItemRepository.findAll(pageRequest)).thenReturn(page);
        when(page.hasContent()).thenReturn(true);
        
        Piece<WorkItem> result = workItemService.getAllByPiece(1, 1);
        assertNotNull(result);
    }

    @Test
    public void canGetAllByCreationDateMocked() {
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        workItemList.add(workItem);
        when(workItemRepository.findByCreationDate(fromDate, toDate)).thenReturn(workItemList);
        
        List<WorkItem> result = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        assertEquals(workItem, result.get(0));
    }

    @Test
    public void canGetById() {
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        WorkItem result = workItemService.getById(workItemId);
        assertEquals(workItem, result);
    }

    @Test
    public void getByIdShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        workItemService.getById(workItemId);
    }

    @Test
    public void getByIdWithNoMatchShouldThrowNoSearchResultException() {
        exception.expect(NotFoundException.class);
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.getById(workItemId);
    }

    @Test
    public void settingInactiveUserToWorkItemShouldThrowException() {
        exception.expect(NotAllowedException.class);
        exception.expectMessage(String.format("User with usernumber '%d' is inactive. Only active User can be assigned to WorkItem", userNumber));
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(false);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void settingUserWithFiveWorkItemToSixthWorkItemShouldThrowException() {
        exception.expect(MaximumQuantityException.class);
        exception.expectMessage("User already have maximum amount of WorkItems");
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItemCollection = createListWithWorkItems(5);
        when(workItemRepository.findByUserId(userId)).thenReturn(workItemCollection);
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
        workItemCollection = createListWithWorkItems(4);
        when(user.getId()).thenReturn(userId);
        when(workItemRepository.findByUserId(userId)).thenReturn(workItemCollection);
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        
        workItemService.setUser(userNumber, workItemId);
        verify(workItem).setUser(user);
        verify(workItemRepository).save(workItem);
    }

    @Test
    public void canSetUserToWorkItemShouldCatchExceptionsAndThrowServiceExeption() {
        exception.expect(ServiceException.class);
        when(userRepository.findByUserNumber(userNumber)).thenThrow(dataAccessException);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void setNotFoundUserToWorkItemShouldThrowNoSearchResultExeption() {
        exception.expect(NotFoundException.class);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(null);
        workItemService.setUser(userNumber, workItemId);
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

        workItemCollection = workItemService.getByUsernumber(userNumber);

        verify(workItemRepository).findByUserId(userId);
        assertEquals(workItemsWithOurUser, workItemCollection);
        workItemCollection.forEach(item -> assertEquals(userId, item.getUser().getId()));
    }

    @Test
    public void getWorkItemsByUserIdShouldThrowExceptionIfNoMatch() {
        exception.expect(NotFoundException.class);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(null);
        workItemService.getByUsernumber(userNumber);
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
                String.format("Cannot remove Issue from WorkItem with id '%d', no Issue in WorkItem.", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        when(workItem.getIssue()).thenReturn(null);
        when(workItem.getId()).thenReturn(workItemId);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemNotFoundInDatabaseShouldThrowException() {
        exception.expect(NotFoundException.class);
        exception.expectMessage(String.format("No WorkItem with id '%d'", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot get WorkItem with id '%d'", workItemId));
        doThrow(dataAccessException).when(workItemRepository).findOne(workItemId);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void canGetAllWithIssue() {
        Collection<WorkItem> workItemsWithIssue = new ArrayList<>();
        workItemsWithIssue.add(workItem);
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(workItemsWithIssue);
        when(workItem.getIssue()).thenReturn(issue);

        workItemCollection = workItemService.getAllWithIssue();
        verify(workItemRepository).findByIssueIsNotNull();
        workItemCollection.forEach(item -> assertNotNull(item.getIssue()));
    }

    @Test
    public void canGetAllWithIssueWhenThereIsNoIssuesShouldReturnEmptyList() {
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(workItemCollection);
        Collection<WorkItem> workItems = workItemCollection = workItemService.getAllWithIssue();
        assertTrue(workItems.isEmpty());
    }

    @Test
    public void canGetAllWithIssueWithNoMatchShouldReturnEmptyList() {
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(new ArrayList<>());
        Collection<WorkItem> workItems = workItemCollection = workItemService.getAllWithIssue();
        assertTrue(workItems.isEmpty());
    }

    @Test
    public void canGetAllWithIssueShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        doThrow(dataAccessException).when(workItemRepository).findByIssueIsNotNull();
        workItemCollection = workItemService.getAllWithIssue();
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
    public void addingNotFoundIssueToWorkItemShouldThrowException() {
        exception.expect(NotFoundException.class);
        exception.expectMessage(String.format("No Issue with id '%d' exists.", issueId));
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void addingIssueNotFoundToWorkItemShouldThrowException() {
        exception.expect(NotFoundException.class);
        exception.expectMessage(String.format("No WorkItem with id '%d' exists.", workItemId));
        when(issueRepository.findOne(issueId)).thenReturn(issue);
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void addingIssueToWorkItemShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot get WorkItem with id '%d'", workItemId));
        doThrow(dataAccessException).when(workItemRepository).findOne(workItemId);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void canPersistIssue() {
        String issueTitle = "This is an issue!";
        workItemService.createIssue(issueTitle);
        verify(issueRepository).save(new Issue(issueTitle));
    }

    @Test
    public void createIssueShouldCatchExceptionsAndThrowServiceException() {
        String issueTitle = "This is an issue!";
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot create Issue with description '%s'", issueTitle));
        doThrow(dataAccessException).when(issueRepository).save(new Issue(issueTitle));
        workItemService.createIssue(issueTitle);
    }

    @Test
    public void canFindByTeamId() {
        workItemCollection.add(workItem);
        when(workItemRepository.findByTeamId(teamId)).thenReturn((List<WorkItem>) workItemCollection);
        workItemService.getByTeamId(teamId);
        verify(workItemRepository).findByTeamId(teamId);
    }

    @Test
    public void canFindByTeamIdShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot not get WorkItems by Team id '%s'", teamId));
        when(workItemRepository.findByTeamId(teamId)).thenThrow(dataAccessException);
        workItemService.getByTeamId(teamId);
    }

    @Test
    public void canFindByTeamIdWithoutMatchShouldReturnEmptyList() {
        when(workItemRepository.findByTeamId(teamId)).thenReturn(new ArrayList<>());
        Collection<WorkItem> workItems = workItemService.getByTeamId(teamId);
        assertTrue(workItems.isEmpty());
        verify(workItemRepository).findByTeamId(teamId);
    }

    @Test
    public void canFindByDescriptionContains() {
        String searchText = "important";
        workItemCollection.add(workItem);
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(workItemCollection);
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByDescriptionContainsWithNoMatchShouldReturnEmptyList() {
        String searchText = "important";
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(workItemCollection);
        Collection<WorkItem> workItems = workItemService.getByDescriptionContains(searchText);
        assertTrue(workItems.isEmpty());
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByStatus() {
        Status wantedStatus = Status.STARTED;
        workItemCollection.add(workItem);
        when(workItemRepository.findByStatus(wantedStatus)).thenReturn(workItemCollection);
        workItemService.getByStatus(wantedStatus);
        verify(workItemRepository).findByStatus(wantedStatus);
    }

    @Test
    public void canFindByStatusShouldReturnEmptyListIfNoWorkItemFound() {
        Status wantedStatus = Status.STARTED;
        when(workItemRepository.findByStatus(wantedStatus)).thenReturn(new ArrayList<>());
        Collection<WorkItem> workItems = workItemService.getByStatus(wantedStatus);
        assertTrue(workItems.isEmpty());
        verify(workItemRepository).findByStatus(wantedStatus);
    }

    @Test
    public void canRemoveWorkItem() {
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItemService.removeById(workItemId);
        verify(workItemRepository).delete(workItem);
    }

    @Test
    public void removeByIdShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        workItemService.removeById(workItemId);
    }

    @Test
    public void removeByIdShouldThrowExceptionWhenWorkItemNotFound() {
        exception.expect(NotFoundException.class);
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.removeById(workItemId);
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
    public void changeWorkItemStatusShouldCatchExceptionsAndThrowServiceException() {
        Status newStatus = Status.DONE;
        exception.expect(DatabaseException.class);
        exception.expectMessage(String.format("Cannot get WorkItem with id '%d'", workItemId));
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        when(workItem.getId()).thenReturn(workItemId);
        workItemService.setStatus(workItemId, newStatus);
    }

    @Test
    public void changeWorkItemStatusOnWorkItemNotFoundShouldThrowNoSearchResultException() {
        exception.expect(NotFoundException.class);
        Status newStatus = Status.DONE;
        when(workItemRepository.findOne(workItem.getId())).thenReturn(null);
        when(workItem.getId()).thenReturn(workItemId);
        workItemService.setStatus(workItem.getId(), newStatus);
    }

    @Test
    public void canCreatePersistedWorkItem() {
        String workItemDescription = "Do something!";
        when(workItemRepository.save(new WorkItem(workItemDescription))).thenReturn(workItem);
        WorkItem result = workItemService.create(workItemDescription);
        verify(workItemRepository).save(new WorkItem(workItemDescription));
        assertEquals(workItem, result);
    }

    @Test
    public void createShouldCatchExceptionsAndThrowDatabaseException() {
        final String workItemDescription = "We should throw exceptions";
        exception.expect(DatabaseException.class);
        exception.expectMessage(String.format("Cannot save WorkItem with description '%s'", workItemDescription));
        when(workItemRepository.save(new WorkItem(workItemDescription))).thenThrow(dataAccessException);
        workItemService.create(workItemDescription);
    }

    @Test
    public void setUserShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        when(userRepository.findByUserNumber(userNumber)).thenReturn(user);
        when(user.isActive()).thenReturn(true);
        when(user.getWorkItems()).thenReturn(workItemCollection);
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void canGetCompletedWorkItemsBetweenDates() {
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        workItemList.add(workItem);
        when(workItemRepository.findByCompletionDate(from, to)).thenReturn(workItemList);
        
        workItemService.getCompletedWorkItems(from, to);
        verify(workItemRepository).findByCompletionDate(from, to);
    }

    @Test
    public void shouldReturnEmptyListIfNoWorkItemsFoundBetweenDates() {
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        when(workItemRepository.findByCompletionDate(from, to)).thenReturn(new ArrayList<>());
        List<WorkItem> workItems = workItemService.getCompletedWorkItems(from, to);
        assertTrue(workItems.isEmpty());
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenGettingCompletedWorkItemBetweenDates() {
        exception.expect(ServiceException.class);
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        doThrow(dataAccessException).when(workItemRepository).findByCompletionDate(from, to);
        workItemService.getCompletedWorkItems(from, to);
    }
}