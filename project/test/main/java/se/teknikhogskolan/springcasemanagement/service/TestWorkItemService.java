package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;

public final class TestWorkItemService {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";

    @Test
    public void canCreatePersistentWorkItem() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();

            WorkItemService workItemService = context.getBean(WorkItemService.class);

            WorkItem workItem = workItemService.createWorkItem("Do something!");

            assertNotNull(workItem.getId());
        }
    }
}
