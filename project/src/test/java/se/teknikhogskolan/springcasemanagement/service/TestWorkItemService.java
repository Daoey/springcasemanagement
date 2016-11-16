package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.exception.ServiceException;

public final class TestWorkItemService {

    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement";
    
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
    private Page<WorkItem> page;

    @InjectMocks
    private WorkItemService workItemService;

    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");
    private final Long workItemId = 235235L;
    private final Long userNumber = 23553L;
    private final Long userId = 589L;
    private final Long teamId = 23353265L;
    private final Long issueId = 23523L;
    private Collection<WorkItem> workItems = new ArrayList<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        workItems.clear();
    }

    @Test
    public void canGetAllByCreationDate() { // TODO make this unit test
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            UserRepository userRepository = context.getBean(UserRepository.class);
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            WorkItemService workItemService = new WorkItemService(workItemRepository, userRepository, issueRepository);

            final int workItemsCreatedToday = 5;
            for (int i = 0; i < workItemsCreatedToday; ++i)
                workItemService.create(String.format("Created today #%d", i));

            LocalDate fromDate = LocalDate.now().minusDays(1);
            LocalDate toDate = LocalDate.now().plusDays(1);
            List<WorkItem> result = workItemService.getByCreatedBetweenDates(fromDate, toDate);
            assertEquals(workItemsCreatedToday, result.size());

            List<WorkItem> items = new ArrayList<>();
            for (int i = 0; i < workItemsCreatedToday; ++i) {
                items.addAll(workItemService.getByDescriptionContains((String.format("Created today #%d", i))));
                workItemService.removeById(items.get(0).getId());
                items.clear();
            }
        }
    }

    @Test
    public void canGetAllBySlices() { // TODO make this unit test
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            UserRepository userRepository = context.getBean(UserRepository.class);
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            WorkItemService workItemService = new WorkItemService(workItemRepository, userRepository, issueRepository);

            int amountOfItems = 10;
            for (int i = 0; i < amountOfItems; ++i)
                workItemService.create(String.format("description #%d", i));

            Page<WorkItem> result = workItemService.getAllByPage(1, 2);
            assertNotNull(result);
            assertEquals(2, result.getSize());

            List<WorkItem> items = new ArrayList<>();
            for (int i = 0; i < amountOfItems; ++i) {
                items.addAll(workItemService.getByDescriptionContains((String.format("description #%d", i))));
                workItemService.removeById(items.get(0).getId());
                items.clear();
            }
        }
    }

    @Test
    public void canGetAllBySlicesMocked() {
        workItems.add(workItem);
        PageRequest pageRequest = new PageRequest(1, 1);
        when(workItemRepository.findAll(pageRequest)).thenReturn(page);
        when(page.hasContent()).thenReturn(true);
        Page<WorkItem> result = workItemService.getAllByPage(1, 1);
        assertEquals(page, result);
    }

    @Test
    public void canGetAllByCreationDateMocked() {
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        List<WorkItem> listToReturn = new ArrayList<>();
        listToReturn.add(workItem);
        when(workItemRepository.findByCreationDate(fromDate, toDate)).thenReturn(listToReturn);
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
        exception.expect(NoSearchResultException.class);
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.getById(workItemId);
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
    public void canSetUserToWorkItemShouldCatchExceptionsAndThrowServiceExeption() {
        exception.expect(ServiceException.class);
        when(userRepository.findByUserNumber(userNumber)).thenThrow(dataAccessException);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void setNotFoundUserToWorkItemShouldThrowNoSearchResultExeption() {
        exception.expect(NoSearchResultException.class);
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

        Collection<WorkItem> result = workItemService.getByUsernumber(userNumber);

        verify(workItemRepository).findByUserId(userId);
        assertEquals(workItemsWithOurUser, result);
        result.forEach(item -> assertEquals(userId, item.getUser().getId()));
    }

    @Test
    public void getWorkItemsByUserIdShouldThrowExceptionIfNoMatch() {
        exception.expect(NoSearchResultException.class);
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
                String.format("Cannot remove Issue from WorkItem %d, no Issue found in WorkItem", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        when(workItem.getIssue()).thenReturn(null);
        when(workItem.getId()).thenReturn(workItemId);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemNotFoundInDatabaseShouldThrowException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("No match for WorkItem with id '%d'", workItemId));
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.removeIssueFromWorkItem(workItemId);
    }

    @Test
    public void removingIssueFromWorkItemShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot get WorkItem '%d'", workItemId));
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
        workItems.forEach(item -> assertNotNull(item.getIssue()));
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
    public void addingNotFoundIssueToWorkItemShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format("No match for Issue with id '%d'", issueId));
        when(issueRepository.findOne(issueId)).thenReturn(null);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void addingIssueNotFoundToWorkItemShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format("No match for WorkItem with id '%d'", workItemId));
        when(issueRepository.findOne(issueId)).thenReturn(issue);
        when(workItemRepository.findOne(workItemId)).thenReturn(null);
        workItemService.addIssueToWorkItem(issueId, workItemId);
    }

    @Test
    public void addingIssueToWorkItemShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        exception.expectMessage(String.format("Cannot get Issue with id '%d'", issueId));
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
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
        workItems.add(workItem);
        when(workItemRepository.findByTeamId(teamId)).thenReturn((List<WorkItem>) workItems);
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
    public void canFindByTeamIdReturnsWithoutResultShouldThrowException() {
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format("No match for WorkItems with team id '%d'", teamId));
        when(workItemRepository.findByTeamId(teamId)).thenReturn(null);
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
    public void canFindByDescriptionContainsReturnsEmptyListShouldThrowException() {
        String searchText = "important";
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format("No match for WorkItem description contains '%s'", searchText));
        when(workItemRepository.findByDescriptionContains(searchText)).thenReturn(workItems);
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByDescriptionContainsReturnsNullShouldThrowException() {
        String searchText = "important";
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format("No match for WorkItem description contains '%s'", searchText));
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
        exception.expect(NoSearchResultException.class);
        exception.expectMessage(String.format(String.format("No match for get WorkItems by Status '%s'", wantedStatus)));
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
    public void removeByIdShouldCatchExceptionsAndThrowServiceException() {
        exception.expect(ServiceException.class);
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        workItemService.removeById(workItemId);
    }

    @Test
    public void removeByIdShouldThrowExceptionWhenWorkItemNotFound() {
        exception.expect(NoSearchResultException.class);
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
        exception.expectMessage(String.format("Cannot get WorkItem '%d'", workItemId));
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        when(workItem.getId()).thenReturn(workItemId);
        workItemService.setStatus(workItemId, newStatus);
    }

    @Test
    public void changeWorkItemStatusOnWorkItemNotFoundShouldThrowNoSearchResultException() {
        exception.expect(NoSearchResultException.class);
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
        when(user.getWorkItems()).thenReturn(workItems);
        when(workItemRepository.findOne(workItemId)).thenThrow(dataAccessException);
        workItemService.setUser(userNumber, workItemId);
    }

    @Test
    public void canGetCompletedWorkItemsBetweenDates() {
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        List<WorkItem> workItems = new ArrayList<>();
        workItems.add(workItem);
        when(workItemRepository.findByCompletionDate(from, to)).thenReturn(workItems);
        workItemService.getCompletedWorkItems(from, to);
        verify(workItemRepository).findByCompletionDate(from, to);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionIfNoWorkItemsFoundBetweenDates() {
        exception.expect(NoSearchResultException.class);
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now().plusDays(1);
        when(workItemRepository.findByCompletionDate(from, to)).thenReturn(null);
        workItemService.getCompletedWorkItems(from, to);
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