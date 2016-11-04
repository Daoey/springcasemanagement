package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
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
    @InjectMocks
    WorkItemService workItemService;

    @Test
    public void canFindByTeamId() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);
            UserService userService = context.getBean(UserService.class);
            TeamService teamService = context.getBean(TeamService.class);
            
            Team team = teamService.saveTeam(new Team("Team finding workitems"));
            User user = new User(656989L, "usernamesadfasdf", "firstName", "lastName", team);
            WorkItem workItem = workItemService.createWorkItem("Find all by team id!");
            user = userService.saveUser(user);
            workItem = workItemService.setUserToWorkItem(user.getUserNumber(), workItem);
            teamService.addUserToTeam(team.getId(), user.getId());
            
            Collection<WorkItem> result = workItemService.getByTeamId(team.getId());
            
            result.forEach(System.out::println);
//            result.forEach(item -> assertEquals(true, item.getDescription().contains(text)));
        }
    }
    
    @Test
    public void canFindByUserId() {
    	// TODO implement with mock
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
