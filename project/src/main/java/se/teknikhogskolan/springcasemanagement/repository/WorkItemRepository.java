package se.teknikhogskolan.springcasemanagement.repository;

import org.springframework.data.repository.CrudRepository;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;

public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {
    
}
