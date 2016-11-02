package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

public final class TestWorkItemService {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";

    // @Test
    // public void canFindByTeamId() {
    // fail("Not implemented, use TeamRepo in WorkItemService");
    // }

    @Test
    public void canFindByUserId() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);
            WorkItem workItem1 = workItemService.createWorkItem("Find all workitems by user!");
            WorkItem workItem2 = workItemService.createWorkItem("Find workitems by user!");

            UserService userService = context.getBean(UserService.class);
            final Long userNumber = 1656L;
            final String username = "Mister Cool";
            User user = new User(userNumber, username, "Per-Erik", "Ferb", null);
            user = userService.saveUser(user);

            workItemService.setUserToWorkItem(userNumber, workItem1);
            workItemService.setUserToWorkItem(userNumber, workItem2);

            Collection<WorkItem> workItems = workItemService.getByUserId(user.getId());

            workItems.forEach(item -> assertEquals(username, item.getUser().getUsername()));
        } catch (Exception e) {
            if (e.getMessage().contains("execute statement; SQL [n/a]; constraint [null]; nested exception is"))
                fail("Duplicated unique userNumber in database, drop and create database between tests");
            throw e;
        }
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
