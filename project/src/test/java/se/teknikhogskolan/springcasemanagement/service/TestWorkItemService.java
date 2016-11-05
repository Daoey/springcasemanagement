package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.repository.WorkItemRepository;

public final class TestWorkItemService {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    @Mock
    WorkItemRepository workItemRepository;
    @Mock
    TeamRepository teamRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    IssueRepository issueRepository;
    @InjectMocks
    WorkItemService workItemService;

    @Test
    public void canFindByUserId() {
        // TODO implement with mock
    }
    
    @Test
    public void canAddIssueToWorkItem() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);
            Issue issue = workItemService.createIssue("Issue must be added to WorkItem");
            WorkItem workItem = workItemService.createWorkItem("WorkItem with an Issue");
            workItem = workItemService.setWorkItemStatus(workItem, Status.DONE);
            workItem = workItemService.addIssueToWorkItem(issue, workItem);

            assertEquals(Status.UNSTARTED, workItem.getStatus());
            assertEquals(issue, workItem.getIssue());
        }
    }

    @Test
    public void canPersistIssue() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            Issue issue = workItemService.createIssue("Issue created by service");

            assertNotNull(issue);
            assertNotNull(issue.getId());
        }
    }

    @Test // Only runs once, unique values in db crashes second run, drop and
    // create db
    public void canFindByTeamId() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);
            UserService userService = context.getBean(UserService.class);
            TeamService teamService = context.getBean(TeamService.class);
            
            Long uniqueUserNumber = getRandomLong();
            String uniqueUsername = "username_" + getRandomLong().toString();
            String uniqueTeamName = "Team finding workitems" + getRandomLong().toString();
            String uniqueWorkItemDescription = "Find all by team id!" + getRandomLong().toString();
            
            Team team = teamService.saveTeam(new Team(uniqueTeamName));
            User user = new User(uniqueUserNumber, uniqueUsername, "firstName", "lastName", team);
            WorkItem workItem = workItemService.createWorkItem(uniqueWorkItemDescription);
            user = userService.saveUser(user);
            workItem = workItemService.setUserToWorkItem(user.getUserNumber(), workItem);
            teamService.addUserToTeam(team.getId(), user.getId());

            Collection<WorkItem> result = workItemService.getByTeamId(team.getId());

            result.forEach(item -> assertEquals(team.getId(), item.getUser().getTeam().getId()));
        }
    }

    private Long getRandomLong() {
        long loverRange = 0;
        long upperRange = 1000000;
        Random random = new Random();
        long randomValue = loverRange + (long) (random.nextDouble() * (upperRange - loverRange));
        return new Long(randomValue);
    }

    @Test
    public void canFindByDescriptionContains() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Find all by description containing something!");
            String text = "Find";
            Collection<WorkItem> result = workItemService.getByDescriptionContains(text);

            result.forEach(item -> assertEquals(true, item.getDescription().contains(text)));
        }
    }

    @Test
    public void canFindByStatus() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Find all with my status!");
            WorkItem.Status status = WorkItem.Status.STARTED;
            workItem = workItemService.setWorkItemStatus(workItem, status);

            Collection<WorkItem> result = workItemService.getByStatus(status);

            result.forEach(item -> assertEquals(status, item.getStatus()));
        }
    }

    @Test
    public void canRemoveWorkItem() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Delete this work item!");

            workItem = workItemService.removeById(workItem.getId());

            WorkItem result = workItemService.getById(workItem.getId());

            assertEquals(null, result);
        }
    }

    @Test
    public void canChangeWorkItemStatus() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Do something else!");

            WorkItem.Status status = WorkItem.Status.STARTED;
            workItem = workItemService.setWorkItemStatus(workItem, status);

            WorkItem result = workItemService.getById(workItem.getId());

            assertEquals(status, result.getStatus());
        }
    }

    @Test
    public void canCreatePersistedWorkItem() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Do something!");

            assertNotNull(workItem.getId());
        }
    }
}
