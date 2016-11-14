package se.teknikhogskolan.springcasemanagement.repository;

import java.time.LocalDate;
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

import static org.junit.Assert.*;

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

    @Test
    public void canGetWorkItemsCompletedBetweenDates() throws Exception {
        WorkItem workItemDone = new WorkItem("Perfect Date1").setStatus(Status.DONE).setCompletionDate(LocalDate.now());
        executeVoid(workItemRepository -> workItemRepository.save(workItemDone));
        WorkItem workItemUnStarted = new WorkItem("Perfect Date2").setStatus(Status.UNSTARTED)
                .setCompletionDate(LocalDate.now());
        executeVoid(workItemRepository -> workItemRepository.save(workItemUnStarted));
        WorkItem workItemStarted = new WorkItem("Perfect Date3").setStatus(Status.STARTED);
        executeVoid(workItemRepository -> workItemRepository.save(workItemStarted));

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        Collection<WorkItem> workItemList = executeMany(
                workItemRepository -> workItemRepository.findByCompletionDate(startDate, toDate));

        assertTrue(workItemList.contains(workItemDone));
        assertFalse(workItemList.contains(workItemUnStarted));
        assertFalse(workItemList.contains(workItemStarted));

        executeVoid(workItemRepository -> {
            workItemRepository.delete(workItemDone);
            workItemRepository.delete(workItemUnStarted);
            workItemRepository.delete(workItemStarted);
        });
    }

    @Test
    public void canGetWorkItemsCreatedBetweenDates() throws Exception {
        WorkItem workItem = new WorkItem("Created today");
        executeVoid(workItemRepository -> workItemRepository.save(workItem));

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(1);
        Collection<WorkItem> workItemsCreatedToday = executeMany(
                workItemRepository -> workItemRepository.findByCreationDate(startDate, toDate));

        assertTrue(workItemsCreatedToday.contains(workItem));

        executeVoid(workItemRepository -> {
            workItemRepository.delete(workItem);
        });
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
