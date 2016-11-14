package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.DONE;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.STARTED;
import static se.teknikhogskolan.springcasemanagement.model.WorkItem.Status.UNSTARTED;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.service.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.ServiceException;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={H2InfrastructureConfig.class})
@Sql(scripts = "add_workitem_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "h2_clean_tables.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class TestWorkItem {    
    @Autowired(required = true)
    private WorkItemService workItemService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final Long workItemInDatabaseId = 98486464L;
    private final String workItemInDatabaseDescription = "Lead TMNT";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Test
    public void canGetByStatus() {
        Collection<WorkItem> result = workItemService.getByStatus(STARTED);
        assertEquals(2, result.size());
    }
    
    @Test
    public void canRemoveWorkItem() {
        exception.expect(NoSearchResultException.class);
        workItemService.removeById(workItemInDatabaseId);
        workItemService.getById(workItemInDatabaseId);
    }
    
    @Test
    public void canGetById() {
        WorkItem result = workItemService.getById(workItemInDatabaseId);
        assertEquals(workItemInDatabaseId, result.getId());
    }
    
    @Test
    public void canRemoveIssueFromWorkItem() {
        Issue issue = workItemService.createIssue("This is an Issue");
        WorkItem workItem = workItemService.getById(workItemInDatabaseId);
        workItem = workItemService.setStatus(workItem.getId(), DONE);
        workItem = workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());
        assertEquals(issue, workItem.getIssue());
        workItem = workItemService.removeIssueFromWorkItem(workItem.getId());
        assertNull(workItem.getIssue());
    }
    
    @Test
    public void canAddIssueToWorkItem() {
        Issue issue = workItemService.createIssue("This is an Issue!");
        WorkItem workItem = workItemService.getById(workItemInDatabaseId);
        workItem = workItemService.setStatus(workItem.getId(), DONE);
        workItem = workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());
        assertEquals(issue, workItem.getIssue());
    }
    
    @Test
    public void canChangeWorkItemStatus() {
        WorkItem workItem = workItemService.getById(workItemInDatabaseId);
        assertEquals(UNSTARTED, workItem.getStatus());
        workItem = workItemService.setStatus(workItem.getId(), STARTED);
        assertEquals(STARTED, workItem.getStatus());
    }
    
    @Test
    public void canCreateIssue() {
        Issue issue = workItemService.createIssue("This is an Issue!");
        assertNotNull(issue.getId());
    }
    
    @Test
    public void canGetAllCreatedBetweenDatesNoMatchShouldThrowNoSearchResultException() {
        exception.expect(NoSearchResultException.class);
        LocalDate fromDate = LocalDate.parse("2016-11-01", formatter);
        LocalDate toDate = LocalDate.parse("2016-11-02", formatter);
        workItemService.getByCreatedBetweenDates(fromDate, toDate);
    }
    
    @Test
    public void canGetAllCreatedBetweenDates() {
        LocalDate fromDate = LocalDate.parse("2016-11-11", formatter);
        LocalDate toDate = LocalDate.parse("2016-11-11", formatter);
        List<WorkItem> result = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        assertHasContent(result);
    }
    
    private static void assertHasContent(Collection<WorkItem> result) {
        assertFalse(result.isEmpty());
    }

    @Test
    public void canGetWorkItemByDescription() {
        Collection<WorkItem> result = workItemService.getByDescriptionContains(workItemInDatabaseDescription);
        assertHasContent(result);
    }
    
    @Test
    public void getWorkItemByDescriptionNoMatchShouldThrowNoSearchResultException() {
        exception.expect(NoSearchResultException.class);
        workItemService.getByDescriptionContains("8r347y8w%%%r78");
    }
    
    @Test
    public void getByCreatedBetweenDatesShouldThrowNoSearchResultExceptionIfNoMatch() {
        exception.expect(NoSearchResultException.class);
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(2);
        workItemService.getByCreatedBetweenDates(fromDate, toDate);
    }
    
    @Test
    public void canGetByCreatedBetweenDates() {
        WorkItem workItem = workItemService.create("Created today #1");
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        List<WorkItem> result = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        assertTrue(result.contains(workItem));
    }
    
    @Test
    public void canCreatePersistentWorkItem() {
        WorkItem result = workItemService.create("description");
        assertNotNull(result.getId());
    }

    @Test
    public void persistingTwoWorkItemsWithSameDescriptionShouldThrowException() {
        exception.expect(ServiceException.class);
        String description = "duplicate descripton";
        workItemService.create(description);
        workItemService.create(description);
    }
}
