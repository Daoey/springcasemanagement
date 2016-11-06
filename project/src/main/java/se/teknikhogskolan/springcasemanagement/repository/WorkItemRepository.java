package se.teknikhogskolan.springcasemanagement.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;

public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {

    Collection<WorkItem> findByStatus(WorkItem.Status status);

    Collection<WorkItem> findByUserId(Long id);

    Collection<WorkItem> findByDescriptionContains(String text);

    Collection<WorkItem> findByIssueIsNotNull();
    
    @Query("Select w from WorkItem w left join User u on w.user.id = u.id WHERE u.team.id = :teamId")
    List<WorkItem> findByTeamId(@Param("teamId") Long teamId);
}
