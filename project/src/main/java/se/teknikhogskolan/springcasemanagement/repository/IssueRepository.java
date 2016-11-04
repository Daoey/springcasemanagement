package se.teknikhogskolan.springcasemanagement.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;
import se.teknikhogskolan.springcasemanagement.model.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {

    Issue findByDescription(String description);

    Slice<Issue> findAllByPage(Pageable pageable);
}
