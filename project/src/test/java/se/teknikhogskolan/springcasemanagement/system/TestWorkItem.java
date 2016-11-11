package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.service.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.ServiceException;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={H2InfrastructureConfig.class})
//@Sql({"workitem_data.sql"})
public class TestWorkItem {    
    @Autowired(required = true)
    private WorkItemService workItemService;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Before
    @Sql(scripts = "classpath:add_workitem_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    public void insertTestData(){
        
    }
    
    @Before
    @Sql(scripts = "classpath:remove_workitem_data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    public void removeTestData(){
        
    }
    
    @Test
    public void testWithSql() {
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
