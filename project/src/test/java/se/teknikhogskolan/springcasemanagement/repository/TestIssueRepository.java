package se.teknikhogskolan.springcasemanagement.repository;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.Issue;

public final class TestIssueRepository {

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private Issue issue;

    @Before
    public void setUp() {
        this.issue = new Issue("No time");
    }

    @Test
    public void canSaveIssue() {
        executeVoid(issueRepository -> issueRepository.save(issue));
        deleteOneIssue(issue);
    }

    @Test
    public void canGetIssueById() {
        Issue issueFromDb = execute(issueRepository -> {
            issue = issueRepository.save(issue);
            return issueRepository.findOne(issue.getId());
        });
        assertEquals(issueFromDb, issue);
        deleteOneIssue(issue);
    }

    @Test
    public void canGetIssueByDescription() {
        String desc = "Description";
        issue.setDescription(desc);
        List<Issue> issuesFromDb = executeMany(issueRepository -> {
            issue = issueRepository.save(issue);
            return issueRepository.findByDescription(desc);
        });

        assertEquals(issuesFromDb.get(0), issue);
        deleteOneIssue(issue);
    }

    private Issue execute(Function<IssueRepository, Issue> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            return operation.apply(issueRepository);
        }
    }

    public void executeVoid(Consumer<IssueRepository> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            operation.accept(issueRepository);
        }
    }

    public List<Issue> executeMany(Function<IssueRepository, List<Issue>> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            return operation.apply(issueRepository);
        }
    }

    private void deleteOneIssue(Issue issue) {
        executeVoid(issueRepository -> issueRepository.delete(issue));
    }
}