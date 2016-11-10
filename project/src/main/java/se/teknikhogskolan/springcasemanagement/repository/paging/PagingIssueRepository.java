package se.teknikhogskolan.springcasemanagement.repository.paging;

import org.springframework.data.repository.PagingAndSortingRepository;
import se.teknikhogskolan.springcasemanagement.model.Issue;

public interface PagingIssueRepository extends PagingAndSortingRepository<Issue, Long> {

}
