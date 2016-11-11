package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.NoSearchResultException;
import se.teknikhogskolan.springcasemanagement.service.ServiceException;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Sql({ "drop_schema.sql", "schema.sql", "data.sql" })
public class TestWorkItem {
    private static AnnotationConfigApplicationContext context;
    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement.config.h2";
    private static WorkItemService workItemService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        context = new AnnotationConfigApplicationContext();
        context.scan(PROJECT_PACKAGE);
        context.refresh();
        WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        IssueRepository issueRepository = context.getBean(IssueRepository.class);
        workItemService = new WorkItemService(workItemRepository, userRepository, issueRepository);
    }

    @AfterClass
    public static void tearDown() {
        context.close();
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
