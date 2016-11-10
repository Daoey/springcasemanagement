package se.teknikhogskolan.springcasemanagement.system;

import org.junit.Before;
import org.junit.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;
import se.teknikhogskolan.springcasemanagement.service.IssueService;


public class TestIssue {

    private IssueService issueService;
    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement.config.hsql";

    @Before
    public void setUp() throws Exception {
        setUpIssueRepository();

    }

    @Test
    public void canGetIssue() throws Exception {

    }

    private IssueRepository setUpIssueRepository(){
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            return context.getBean(IssueRepository.class);
        }
    }
}
