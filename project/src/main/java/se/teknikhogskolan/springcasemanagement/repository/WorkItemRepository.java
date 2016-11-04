package se.teknikhogskolan.springcasemanagement.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;

public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {
    
    Collection<WorkItem> findByStatus(WorkItem.Status status);
    
    Collection<WorkItem> findByUserId(Long id);
        
    Collection<WorkItem> findByDescriptionContains(String text);
    
//    @Query("SELECT w FROM WorkItem LEFT JOIN User ON User = WorkItem.user WHERE User.team.id = :teamId")
//    Collection<WorkItem> findByTeamId(@Param("teamId") Long teamId);
}
