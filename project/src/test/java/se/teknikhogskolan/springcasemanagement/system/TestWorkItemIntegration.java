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
import java.util.ArrayList;
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
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;
import se.teknikhogskolan.springcasemanagement.service.exception.InvalidInputException;
import se.teknikhogskolan.springcasemanagement.service.exception.MaximumQuantityException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;
import se.teknikhogskolan.springcasemanagement.service.exception.ServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { H2InfrastructureConfig.class })
@SqlGroup({
    @Sql(scripts = "add_workitem_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "h2_clean_tables.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
public class TestWorkItemIntegration {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private WorkItemService workItemService;

    private final Long workItemLeadTeamId = 98486464L;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void duplicateKeysShouldThrowException() {
        final String description = "duplicate description";
        exception.expect(InvalidInputException.class);
        exception.expectMessage(String.format("WorkItem with description '%s' violates data integrity", description));
        workItemService.create(description);
        workItemService.create(description);
    }

    @Test
    public void workItemToString() {
        final Long workItemWithIssueAndUser = 12343456L;
        final String expectedToString = "WorkItem [id=12343456, description=Lead TMNT, status=STARTED, issueId=123541, "
                                        + "userId=68165, created=2016-11-11, lastModified=null, completionDate=null]";
        String result = workItemService.getById(workItemWithIssueAndUser).toString();
        assertEquals(result, expectedToString);
    }

    @Test
    public void canGetWorkItemsWithIssue() {
        Collection<WorkItem> result = workItemService.getAllWithIssue();
        final int workItemsWithIssue = 1;
        assertEquals(workItemsWithIssue, result.size());
    }

    @Test
    public void canGetByUser() {
        final Long userNumber = 10003L;
        List<WorkItem> result = new ArrayList<>();
        result.addAll(workItemService.getByUsernumber(userNumber));
        final Long workItemId = 98486464L;
        assertEquals(workItemId, result.get(0).getId());
    }

    @Test
    public void canGetByTeam() {
        final Long teamId = 2465878L;
        Collection<WorkItem> result = workItemService.getByTeamId(teamId);
        final int workItemsInTeam = 2;
        assertEquals(workItemsInTeam, result.size());
    }

    @Test
    public void canAddWorkItemToUser() {
        Long workItemIdWithoutUser = 8658766L;
        Long userNumberWithoutWorkItem = 10002L;
        WorkItem workItem = workItemService.getById(workItemIdWithoutUser);
        assertNull(workItem.getUser());
        workItem = workItemService.setUser(userNumberWithoutWorkItem, workItemIdWithoutUser);
        assertEquals(userNumberWithoutWorkItem, workItem.getUser().getUserNumber());
    }

    @Test
    public void addingWorkItemToInactiveUserShouldThrowException() {
        exception.expect(InvalidInputException.class);
        exception.expectMessage("User is inactive. Only active User can be assigned to WorkItem");
        Long workItemIdWithoutUser = 8658766L;
        Long inactiveUsernumber = 20001L;
        workItemService.setUser(inactiveUsernumber, workItemIdWithoutUser);
    }

    @Test
    public void addingWorkItemToUserWithFiveWorkItemsShouldThrowException() {
        exception.expect(MaximumQuantityException.class);
        exception.expectMessage("User already have maximum amount of WorkItems");
        Long workItemIdWithoutUser = 8658766L;
        Long usernumberWithFiveWorkItems = 20002L;
        workItemService.setUser(usernumberWithFiveWorkItems, workItemIdWithoutUser);
    }

    @Test
    public void canGetByStatus() {
        final int workItemsStarted = 3;
        Collection<WorkItem> result = workItemService.getByStatus(STARTED);
        assertEquals(workItemsStarted, result.size());
    }

    @Test
    public void canRemoveWorkItem() {
        exception.expect(NotFoundException.class);
        workItemService.removeById(workItemLeadTeamId);
        workItemService.getById(workItemLeadTeamId);
    }

    @Test
    public void canGetById() {
        WorkItem result = workItemService.getById(workItemLeadTeamId);
        assertEquals(workItemLeadTeamId, result.getId());
    }

    @Test
    public void canRemoveIssueFromWorkItem() {
        Issue issue = workItemService.createIssue("This is an Issue");
        WorkItem workItem = workItemService.getById(workItemLeadTeamId);
        workItem = workItemService.setStatus(workItem.getId(), DONE);
        workItem = workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());
        assertEquals(issue, workItem.getIssue());
        workItem = workItemService.removeIssueFromWorkItem(workItem.getId());
        assertNull(workItem.getIssue());
    }

    @Test
    public void canAddIssueToWorkItem() {
        Issue issue = workItemService.createIssue("This is an Issue!");
        WorkItem workItem = workItemService.getById(workItemLeadTeamId);
        workItem = workItemService.setStatus(workItem.getId(), DONE);
        workItem = workItemService.addIssueToWorkItem(issue.getId(), workItem.getId());
        assertEquals(issue, workItem.getIssue());
    }

    @Test
    public void canChangeWorkItemStatus() {
        WorkItem workItem = workItemService.getById(workItemLeadTeamId);
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
        exception.expect(NotFoundException.class);
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
        final String workItemLeadTeamDescription = "Lead TMNT";
        Collection<WorkItem> result = workItemService.getByDescriptionContains(workItemLeadTeamDescription);
        assertHasContent(result);
    }

    @Test
    public void getWorkItemByDescriptionNoMatchShouldThrowNoSearchResultException() {
        exception.expect(NotFoundException.class);
        workItemService.getByDescriptionContains("8r347y8w%%%r78");
    }

    @Test
    public void getByCreatedBetweenDatesShouldThrowNoSearchResultExceptionIfNoMatch() {
        exception.expect(NotFoundException.class);
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