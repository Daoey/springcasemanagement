package se.teknikhogskolan.springcasemanagement.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.User;

public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {

    Page<WorkItem> findAll(Pageable pageable);

    Collection<WorkItem> findByStatus(WorkItem.Status status);

    Collection<WorkItem> findByUserId(Long userId);

    Collection<WorkItem> findByDescriptionContains(String text);

    Collection<WorkItem> findByIssueIsNotNull();

    @Query("Select w from WorkItem w left join User u on w.user.id = u.id WHERE u.team.id = :teamId")
    List<WorkItem> findByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT w FROM WorkItem w WHERE w.completionDate BETWEEN :startDate AND :endDate AND w.status = 'DONE'")
    List<WorkItem> findByCompletionDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM WorkItem w WHERE w.created BETWEEN :startDate AND :endDate")
    List<WorkItem> findByCreationDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
