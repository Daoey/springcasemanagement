package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.service.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.ServiceException;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={H2InfrastructureConfig.class})
@Sql(scripts = "add_workitem_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "remove_workitem_data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class TestWorkItem {    
    @Autowired(required = true)
    private WorkItemService workItemService;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Before
    public void insertTestData(){}
    
    @After
    public void removeTestData(){}

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
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
        Collection<WorkItem> result = workItemService.getByDescriptionContains("1");
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
    @Rollback
    public void canGetByCreatedBetweenDates() {
        WorkItem workItem = workItemService.create("Created today #1");
        LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        List<WorkItem> result = workItemService.getByCreatedBetweenDates(fromDate, toDate);
        assertTrue(result.contains(workItem));
    }
    
    @Test
    @Rollback
    public void canCreatePersistentWorkItem() {
        WorkItem result = workItemService.create("description");
        assertNotNull(result.getId());
    }

    @Test
    @Rollback
    public void persistingTwoWorkItemsWithSameDescriptionShouldThrowException() {
        exception.expect(ServiceException.class);
        String description = "duplicate descripton";
        workItemService.create(description);
        workItemService.create(description);
    }
}
