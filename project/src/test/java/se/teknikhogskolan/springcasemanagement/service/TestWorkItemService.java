package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

public final class TestWorkItemService {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    
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

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void canGetWorkItemsByUserId() {
        WorkItem workItem = new WorkItem("sdfgsdfgdsfg");
        workItem.setUser(user);
        Collection<WorkItem> workItemsWithOurUser = new ArrayList<>();
        workItemsWithOurUser.add(workItem);
        Long userId = 23424L;
        Long userNumber = 23424L;
        
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
        Long workItemId = 646532L;
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
    public void canGetAllWithIssue() {
        Collection<WorkItem> workItemsWithIssue = new ArrayList<>();
        workItemsWithIssue.add(workItem);
        
        when(workItemRepository.findByIssueIsNotNull()).thenReturn(workItemsWithIssue);
        when(workItem.getIssue()).thenReturn(issue);
        
        Collection<WorkItem> workItems = workItemService.getAllWithIssue();
        
        verify(workItemRepository).findByIssueIsNotNull();
        workItems.forEach(item -> {
            assertNotNull(item.getIssue());
        });
    }
    
    @Test
    public void addingIssueToWorkItemWithWrongStatusShouldThrowException() {
        Status wrongStatus = Status.STARTED;
        exception.expect(ServiceException.class);
        exception.expectMessage("Issue can only be added to WorkItem with Status DONE, Status was " + wrongStatus);
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
    public void canPersistIssue() {
        String issueTitle = "This is an issue!";
        workItemService.createIssue(issueTitle);
        verify(issueRepository).save(new Issue(issueTitle));
    }

    @Test
    public void canFindByTeamId() {
        Long teamId = 23353265L;
        workItemService.getByTeamId(teamId);
        verify(workItemRepository).findByTeamId(teamId);
    }
    
    @Test
    public void canFindByDescriptionContains() {
        String searchText = "important";
        workItemService.getByDescriptionContains(searchText);
        verify(workItemRepository).findByDescriptionContains(searchText);
    }

    @Test
    public void canFindByStatus() {
        Status wantedStatus = Status.STARTED;
        workItemService.getByStatus(wantedStatus);
        verify(workItemRepository).findByStatus(wantedStatus);
    }

    @Test
    public void canRemoveWorkItem() {
        Long workItemId = 233690L;
        when(workItemRepository.findOne(workItemId)).thenReturn(workItem);
        workItemService.removeById(workItemId);
        verify(workItemRepository).delete(workItem);
    }

    @Test
    public void canChangeWorkItemStatus() {
        Status newStatus = Status.DONE;
        when(workItemRepository.findOne(workItem.getId())).thenReturn(workItem);
        workItemService.setStatus(workItem.getId(), newStatus);// TODO workItemId
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
