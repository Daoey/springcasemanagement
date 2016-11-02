package se.teknikhogskolan.springcasemanagement.repository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.teknikhogskolan.springcasemanagement.model.Issue;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public final class TestIssueRepository {

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private Issue issue;

    @Before
    public void setUp() throws Exception {
        this.issue = new Issue("No time");
    }

    @Test
    public void canSaveIssue() throws Exception {
        executeVoid(issueRepository -> issueRepository.save(issue));
        deleteIssue(issue);
    }

    @Test
    public void canGetIssue() throws Exception {
        Issue issueFromDb = execute(issueRepository -> {
            issueRepository.save(issue);
            return issueRepository.findOne(issue.getId());
        });
        assertEquals(issue, issueFromDb);
        deleteIssue(issue);
    }

    private Issue execute(Function<IssueRepository, Issue> operation){
        try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()){
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            return operation.apply(issueRepository);
        }
    }

    private void executeVoid(Consumer<IssueRepository> operation){
        try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()){
            context.scan(projectPackage);
            context.refresh();
            IssueRepository issueRepository = context.getBean(IssueRepository.class);
            operation.accept(issueRepository);
        }
    }

    private void deleteIssue(Issue issue){
        executeVoid(issueRepository -> issueRepository.delete(issue));
    }
}