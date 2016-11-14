package se.teknikhogskolan.springcasemanagement.repository.paging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.TestIssueRepository;

public final class TestPagingIssueRepository {

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private TestIssueRepository testIssueRepository;

    @Before
    public void setUp() throws Exception {
        testIssueRepository = new TestIssueRepository();
    }

    @Test
    public void canGetIssueByPage() {
        int pageSize = 7;
        List<Issue> issuesInDb = addIssuesToDb(10);
        Page<Issue> issuePage = executeMany(
                pagingIssueRepository -> pagingIssueRepository.findAll(new PageRequest(0, pageSize)));

        issuePage.forEach(issue -> assertTrue(issuesInDb.contains(issue)));
        assertEquals(issuePage.getSize(), pageSize);
        deleteManyIssues(issuesInDb);
    }

    private Page<Issue> executeMany(Function<PagingIssueRepository, Page<Issue>> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            PagingIssueRepository pagingIssueRepository = context.getBean(PagingIssueRepository.class);
            return operation.apply(pagingIssueRepository);
        }
    }

    private void deleteManyIssues(List<Issue> issues) {
        testIssueRepository.executeVoid(issueRepository -> issueRepository.delete(issues));
    }

    private List<Issue> addIssuesToDb(int amount) {
        List<Issue> issuesInDb = new ArrayList<>();
        testIssueRepository.executeVoid(issueRepository -> {
            for (int i = 0; i < amount; i++) {
                Issue issue = new Issue("test");
                issuesInDb.add(issue);
                issueRepository.save(issue);
            }
        });
        return issuesInDb;
    }
}