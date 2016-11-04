package se.teknikhogskolan.springcasemanagement.repository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.service.ServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public final class TestIssueRepository {

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private Issue issue;

    @Before
    public void setUp() {
        this.issue = new Issue("No time");
    }

    @Test
    public void canSaveIssue() throws ServiceException {
        executeVoid(issueRepository -> issueRepository.save(issue));
        deleteOneIssue(issue);
    }

    @Test
    public void canGetIssueById() throws ServiceException {
        Issue issueFromDb = execute(issueRepository -> {
            issue = issueRepository.save(issue);
            return issueRepository.findOne(issue.getId());
        });
        assertEquals(issueFromDb, issue);
        deleteOneIssue(issue);
    }

    @Test
    public void canGetIssueByDescription() throws ServiceException {
        String desc = "Description";
        issue.setDescription(desc);
        Issue issueFromDb = execute(issueRepository -> {
            issue = issueRepository.save(issue);
            return issueRepository.findByDescription(desc);
        });

        assertEquals(issueFromDb, issue);
        deleteOneIssue(issue);
    }

    @Test
    public void canGetIssueByPage() throws Exception {
        List<Issue> issuesInDb = addIssuesToDb(10);
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);

            Slice<Issue> issueSlice = issueRepository.findAllByPage(new PageRequest(0, 7));
            issueSlice.forEach(issue -> System.out.println(issue.getId()));
        }
        deleteManyIssues(issuesInDb);
    }

    private Issue execute(Function<IssueRepository, Issue> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            return operation.apply(issueRepository);
        }
    }

    private void executeVoid(Consumer<IssueRepository> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            operation.accept(issueRepository);
        }
    }

    private void deleteOneIssue(Issue issue) {
        executeVoid(issueRepository -> issueRepository.delete(issue));
    }

    private void deleteManyIssues(List<Issue> issues) {
        executeVoid(issueRepository -> issueRepository.delete(issues));
    }

    private List<Issue> addIssuesToDb(int amount) {
        List<Issue> issuesInDb = new ArrayList<>();
        executeVoid(issueRepository -> {
            for (int i = 0; i < amount; i++) {
                Issue issue = new Issue("test");
                issuesInDb.add(issue);
                issueRepository.save(issue);
            }
        });
        return issuesInDb;
    }
}