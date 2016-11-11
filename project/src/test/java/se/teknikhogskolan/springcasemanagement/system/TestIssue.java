package se.teknikhogskolan.springcasemanagement.system;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.service.IssueService;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = H2InfrastructureConfig.class)
@SqlGroup({
        @Sql(scripts = "insert_issue.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "delete_issue.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class TestIssue {

    private Long issueId;
    private String issueDescription;

    @Autowired
    private IssueService issueService;

    @Before
    public void setUp() throws Exception {
        this.issueId = 1L;
        this.issueDescription = "Description";
    }

    @Test
    public void canGetIssue() throws Exception {
        Issue issue = issueService.getById(issueId);
        assertNotNull(issue);
    }
}
