package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.paging.PagingIssueRepository;

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
        Issue issue;
        try {
            issue = issueRepository.findOne(issueId);
        } catch (Exception e) {
            throw new ServiceException("Could not get issue with id: " + issueId, e);
        }

        if (issue != null) {
            return issue;
        } else
            throw new NoSearchResultException("Issue with id '" + issueId + "' do not exist");
    }

    public List<Issue> getByDescription(String description) {
        List<Issue> issue;
        try {
            issue = issueRepository.findByDescription(description);
        } catch (Exception e) {
            throw new ServiceException("Could not get issues with description: " + description, e);
        }

        if (issue != null) {
            return issue;
        } else
            throw new NoSearchResultException("Issues with description '" + description + "' do not exist");
    }

    public Issue updateDescription(Long issueId, String description) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            if (issue.isActive()) {
                issue.setDescription(description);
                return issueRepository.save(issue);
            } else {
                throw new ServiceException("Could not update "
                        + "description on Issue with id '" + issueId + "' since it's inactivate.");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Failed to update issue with id '"
                    + issueId + "' since it could not be found in the database", e);
        } catch (Exception e) {
            throw new ServiceException("Could not update description on issue with id: " + issueId, e);
        }
    }

    public Issue inactivate(Long issueId) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            issue.setActive(false);
            return issueRepository.save(issue);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Failed to inactivate issue with id '"
                    + issueId + "' since it could not be found in the database", e);
        } catch (Exception e) {
            throw new ServiceException("Could not inactivate issue with id: " + issueId, e);
        }
    }

    public Issue activate(Long issueId) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            issue.setActive(true);
            return issueRepository.save(issue);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Failed to activate issue with id '"
                    + issueId + "' since it could not be found in the database");
        } catch (Exception e) {
            throw new ServiceException("Could not activate issue with id: " + issueId, e);
        }
    }

    public Page<Issue> getAllByPage(int pageNumber, int pageSize) {
        Page<Issue> page;
        try {
            page = pagingIssueRepository.findAll(new PageRequest(pageNumber, pageSize));
        } catch (Exception e) {
            throw new ServiceException("Could not get issues by page", e);
        }

        if (page != null) {
            return page;
        } else
            throw new NoSearchResultException("No issues on page: " + pageNumber);
    }
}