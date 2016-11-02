package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;

public final class TestWorkItemService {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";

    @Test
    public void canFindByStatus() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Find all with my status!");
            WorkItem.Status status = WorkItem.Status.STARTED;
            workItem = workItemService.setWorkItemStatus(workItem, status);

            Collection<WorkItem> result = workItemService.findByStatus(status);

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
