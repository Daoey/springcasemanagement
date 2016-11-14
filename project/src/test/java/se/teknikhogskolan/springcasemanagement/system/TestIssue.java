package se.teknikhogskolan.springcasemanagement.system;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.teknikhogskolan.springcasemanagement.config.h2.H2InfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.service.IssueService;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = H2InfrastructureConfig.class)
@SqlGroup({
        @Sql(scripts = "insert_issue.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "delete_issue.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class TestIssue {

    private Long issueId;
    private Issue issueInDb;
    private List<Issue> issuesFromPageOne;

    @Autowired
    private IssueService issueService;

    @Before
    public void setUp() {
        this.issueId = 1L;
        this.issueInDb = new Issue("Description");
        this.issuesFromPageOne = issueService.getByDescription("page1");
    }

    @Test
    public void canGetIssue() {
        Issue issueFromDb = issueService.getById(issueId);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test
    public void canGetByDescription() {
        List<Issue> issueFromDb = issueService.getByDescription(issueInDb.getDescription());
        assertEquals(issueFromDb.get(0), issueInDb);
    }

    @Test
    public void canUpdateDescription() {
        String newDescription = "New description";
        Issue issueFromDb = issueService.updateDescription(issueId, newDescription);
        assertEquals(issueFromDb.getDescription(), newDescription);
    }

    @Test
    public void canInactivateIssue() {
        Issue issueFromDb = issueService.inactivate(issueId);
        assertFalse(issueFromDb.isActive());
    }

    @Test
    public void canActivateIssue() {
        Issue inactiveIssue = issueService.inactivate(issueId);
        assertFalse(inactiveIssue.isActive());
        Issue activeIssue = issueService.activate(issueId);
        assertTrue(activeIssue.isActive());
    }

    @Test
    public void canGetAllByPage() {
        Page<Issue> issuePage = issueService.getAllByPage(1, 2);
        assertEquals(issuePage.getSize(), issuesFromPageOne.size());
    }
}
