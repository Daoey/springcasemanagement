package se.teknikhogskolan.springcasemanagement.repository;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;

public final class TestWorkItemRepository {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement"; 

    @Test
    public void canGetWorkItem() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan(projectPackage);
        context.refresh();
        WorkItemRepository repository = context.getBean(WorkItemRepository.class);
        WorkItem workItem = new WorkItem("Do laundry");
        workItem.setStatus(Status.STARTED);
        workItem = repository.save(workItem);
        WorkItem result = repository.findOne(workItem.getId());
        assertEquals(workItem, result);
        context.close();
    }

    @Test
    public void canSaveWorkItemWithStatus() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan(projectPackage);
        context.refresh();
        WorkItemRepository repository = context.getBean(WorkItemRepository.class);
        WorkItem workItem = new WorkItem("Do the vacuumer");
        workItem.setStatus(Status.DONE);
        repository.save(workItem);
        context.close();
    }

    @Test
    public void canSaveWorkItem() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan(projectPackage);
        context.refresh();
        WorkItemRepository repository = context.getBean(WorkItemRepository.class);
        WorkItem workItem = new WorkItem("Do dishes");
        repository.save(workItem);
        context.close();
    }
}
