package se.teknikhogskolan.springcasemanagement.repository;


import static org.junit.Assert.*;

import java.util.Collection;
import java.util.function.Function;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;

public final class TestWorkItemRepository {
    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    
    @Test
    public void canRemoveWorkItem(){
    	execute(repo -> {
    		
    		WorkItem workItem = repo.save(new WorkItem("Show us how to remove!"));
    		assertNotNull(repo.findOne(workItem.getId()));
    		
    		repo.delete(workItem.getId());
    		assertNull(repo.findOne(workItem.getId()));
    		
    		return null;
    	});
    }

    @Test
    public void canChangeWorkItemStatus() {
    	Status changedStatus = Status.DONE;
    	
        WorkItem result = execute(repo -> {
        	
        	WorkItem workItem = repo.save(new WorkItem("Change the status on WorkItem"));
            assertEquals(Status.UNSTARTED, workItem.getStatus()); // UNSTARTED = default status
            
            workItem.setStatus(changedStatus);
            workItem = repo.save(workItem);
            
        	return repo.findOne(workItem.getId());
        });
        
        assertEquals(changedStatus, result.getStatus());
    }

    @Test
    public void canGetWorkItemsByStatus() {
    	Status status = Status.STARTED;
        WorkItem workItem = new WorkItem("Do laundry");
        workItem.setStatus(status);
        Collection<WorkItem> result = executeMany(repo -> {
        	repo.save(workItem);
        	return repo.findByStatus(status);
        });
        result.forEach(item -> assertEquals(status, item.getStatus()));
    }

    @Test
    public void canSaveWorkItemWithStatus() {
    	Status status = Status.DONE;
        WorkItem workItem = new WorkItem("Do the vacuumer");
        workItem.setStatus(status);
        WorkItem result = execute(repo -> {
        	return repo.save(workItem);
        });
        assertEquals(status, result.getStatus());
    }

    @Test
    public void canPersistWorkItem() {
        WorkItem workItem = execute(workItemRepository -> {
            return workItemRepository.save(new WorkItem("Do dishes"));
        });
        assertNotNull(workItem.getId());
    }

    private WorkItem execute(Function<WorkItemRepository, WorkItem> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            return operation.apply(workItemRepository);
        }
    }

    private Collection<WorkItem> executeMany(Function<WorkItemRepository, Collection<WorkItem>> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            WorkItemRepository workItemRepository = context.getBean(WorkItemRepository.class);
            return operation.apply(workItemRepository);
        }
    }
}
