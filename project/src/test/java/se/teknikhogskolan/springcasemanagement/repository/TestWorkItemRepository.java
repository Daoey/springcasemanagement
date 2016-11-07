package se.teknikhogskolan.springcasemanagement.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;

public final class TestWorkItemRepository {
    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement";
    private static WorkItem workItem;
    private static WorkItem workItemDone;
    private static Team team;
    private static User user;

    @BeforeClass
    public static void masterSetup() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            
            team = context.getBean(TeamRepository.class).save(new Team("Team with WorkItems"));
            
            UserRepository userRepository = context.getBean(UserRepository.class);
            user = userRepository.save(new User(23142134L, "Team_working_guy", "Test", "Tester"));
            user.setTeam(team);
            user = userRepository.save(user);
            
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            workItem = workItemRepository.save(new WorkItem("Test getting all WorkItems from one Team").setUser(user));
            
            Status status = Status.DONE;
            workItemDone = new WorkItem("Do the vacuumer");
            workItemDone.setStatus(status);
            workItemDone = workItemRepository.save(workItemDone);
        }
    }

    @AfterClass
    public static void masterTearDown() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            workItemRepository.delete(workItem.getId());
            workItemRepository.delete(workItemDone.getId());
            context.getBean(UserRepository.class).delete(user.getId());
            context.getBean(TeamRepository.class).delete(team.getId());
        }
    }

    @Test
    public void canGetWorkItemsByTeamId() {
        Long teamIdInDb = team.getId();

        Collection<WorkItem> result = executeMany(repo -> {
            return repo.findByTeamId(teamIdInDb);
        });

        assertFalse(result.isEmpty());
        result.forEach(workItem -> {
            Long teamId = workItem.getUser().getTeam().getId();
            assertEquals(teamIdInDb, teamId);
        });
    }

    @Test
    public void canRemoveWorkItem() {
        executeVoid(repo -> {

            WorkItem workItem = repo.save(new WorkItem("Show us how to remove!"));
            assertNotNull(repo.findOne(workItem.getId()));

            repo.delete(workItem.getId());
            assertNull(repo.findOne(workItem.getId()));
        });
    }

    @Test
    public void canChangeWorkItemStatus() {
        Status defaultStatus = Status.UNSTARTED;
        Status changedStatus = Status.DONE;

        executeVoid(repo -> {

            WorkItem workItem = repo.findOne(this.workItem.getId());
            assertEquals(defaultStatus, workItem.getStatus());

            workItem.setStatus(changedStatus);
            workItem = repo.save(workItem);

            WorkItem result = repo.findOne(this.workItem.getId());
            assertEquals(changedStatus, result.getStatus());
        });
    }

    @Test
    public void canGetWorkItemsByStatus() {
        Status statusDone = workItemDone.getStatus();

        Collection<WorkItem> result = executeMany(repo -> {
            return repo.findByStatus(statusDone);
        });

        assertFalse(result.isEmpty());
        result.forEach(item -> assertEquals(statusDone, item.getStatus()));
    }

    private void executeVoid(Consumer<WorkItemRepository> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            operation.accept(workItemRepository);
        }
    }

    private Collection<WorkItem> executeMany(Function<WorkItemRepository, Collection<WorkItem>> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            return operation.apply(workItemRepository);
        }
    }
}
