package se.teknikhogskolan.springcasemanagement.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import se.teknikhogskolan.springcasemanagement.model.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {

    List<Issue> findByDescription(String description);
}
