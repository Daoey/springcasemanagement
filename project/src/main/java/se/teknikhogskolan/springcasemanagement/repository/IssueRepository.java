package se.teknikhogskolan.springcasemanagement.repository;

import org.springframework.data.repository.CrudRepository;
import se.teknikhogskolan.springcasemanagement.model.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {

}
