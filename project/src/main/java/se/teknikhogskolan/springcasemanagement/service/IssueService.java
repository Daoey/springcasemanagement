package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.paging.PagingIssueRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotAllowedException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final PagingIssueRepository pagingIssueRepository;

    @Autowired
    public IssueService(IssueRepository issueRepository, PagingIssueRepository pagingIssueRepository) {
        this.issueRepository = issueRepository;
        this.pagingIssueRepository = pagingIssueRepository;
    }

    public Issue getById(Long issueId) {
        Issue issue = findIssue(issueId);

        if (issue != null) {
            return issue;
        } else
            throw new NotFoundException("Issue with id '" + issueId + "' do not exist");
    }

    public List<Issue> getByDescription(String description) {
        List<Issue> issue;
        try {
            issue = issueRepository.findByDescription(description);
        } catch (DataAccessException e) {
            throw new DatabaseException("Could not get issues with description: " + description, e);
        }

        if (issue != null) {
            return issue;
        } else
            throw new NotFoundException("Issues with description '" + description + "' do not exist");
    }

    public Issue updateDescription(Long issueId, String description) {
        Issue issue = findIssue(issueId);
        if (issue != null) {
            if (issue.isActive()) {
                issue.setDescription(description);
                return saveIssue(issue, "Could not update description on issue with id: " + issueId);
            } else {
                throw new NotAllowedException("Could not update "
                        + "description on Issue with id '" + issueId + "' since it's inactivate.");
            }
        } else {
            throw new NotFoundException("Failed to update issue with id '"
                    + issueId + "' since it could not be found in the database");
        }
    }

    public Issue inactivate(Long issueId) {
        Issue issue = findIssue(issueId);
        if (issue != null) {
            issue.setActive(false);
            return saveIssue(issue, "Could not inactivate issue with id: " + issueId);
        } else {
            throw new NotFoundException("Failed to inactivate issue with id '"
                    + issueId + "' since it could not be found in the database");
        }
    }

    public Issue activate(Long issueId) {
        Issue issue = findIssue(issueId);
        if (issue != null) {
            issue.setActive(true);
            return saveIssue(issue, "Could not activate issue with id: " + issueId);
        } else {
            throw new NotFoundException("Failed to activate issue with id '"
                    + issueId + "' since it could not be found in the database");
        }
    }

    public Page<Issue> getAllByPage(int pageNumber, int pageSize) {
        Page<Issue> page;
        try {
            page = pagingIssueRepository.findAll(new PageRequest(pageNumber, pageSize));
        } catch (DataAccessException e) {
            throw new DatabaseException("Could not get issues by page", e);
        }

        if (page != null) {
            return page;
        } else
            throw new NotFoundException("No issues on page: " + pageNumber);
    }

    private Issue saveIssue(Issue issue, String exceptionMessage) {
        try {
            return issueRepository.save(issue);
        } catch (DataAccessException e) {
            throw new DatabaseException(exceptionMessage, e);
        }
    }

    private Issue findIssue(Long issueId) {
        try {
            return issueRepository.findOne(issueId);
        } catch (DataAccessException e) {
            throw new DatabaseException("Could not find issue with id: " + issueId, e);
        }
    }
}