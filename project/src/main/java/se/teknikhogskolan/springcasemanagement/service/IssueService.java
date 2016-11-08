package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;

@Service
public class IssueService {

    private final IssueRepository issueRepository;

    @Autowired
    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
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
            throw new NoSearchResultException("Issue with issueId '" + issueId + "' do not exist");
    }

    public Issue getByDescription(String description) {
        Issue issue;
        try {
            issue = issueRepository.findByDescription(description);
        } catch (Exception e) {
            throw new ServiceException("Could not get issue with description: " + description, e);
        }

        if (issue != null) {
            return issue;
        } else
            throw new NoSearchResultException("Issue with description '" + description + "' do not exist");
    }

    public Issue updateDescription(Long issueId, String description) {
        try {
            Issue issue = issueRepository.findOne(issueId);
            if (issue.isActive()) {
                issue.setDescription(description);
                return issueRepository.save(issue);
            } else {
                throw new ServiceException("Could not update "
                        + "description on Issue with issueId '" + issueId + "' since it's inactivate.");
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
            throw new NoSearchResultException("Failed to inactive issue with id '"
                    + issueId + "' since it could not be found in the database", e);
        } catch (Exception e) {
            throw new ServiceException("Could not inactive issue with id: " + issueId, e);
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
            throw new ServiceException("Could not active issue with id: " + issueId, e);
        }
    }

    public Slice<Issue> getAllByPage(int page, int pageSize) {
        Slice<Issue> slice;
        try {
            slice = issueRepository.findAll(new PageRequest(page, pageSize));
        } catch (Exception e) {
            throw new ServiceException("Could not get issues by page", e);
        }

        if (slice != null) {
            return slice;
        } else
            throw new NoSearchResultException("no issues on page: " + page);
    }
}