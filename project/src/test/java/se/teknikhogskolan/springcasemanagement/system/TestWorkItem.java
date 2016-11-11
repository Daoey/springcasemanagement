package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

public class TestWorkItem {
	private static AnnotationConfigApplicationContext context;
	private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement.config.h2";
	private static WorkItemService workItemService;
	
	@BeforeClass
	public static void setup(){
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
	public void canCreatePersistentWorkItem() {
		WorkItem result = workItemService.create("description");
		assertNotNull(result.getId());
	}

}
