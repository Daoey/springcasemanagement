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
        Issue issue = issueRepository.findOne(issueId);
        if (issue != null) {
            return issue;
        } else
            throw new ServiceException("Issue with issueId '" + issueId + "' do not exist");
    }

    public Issue getByDescription(String description) {
        Issue issue = issueRepository.findByDescription(description);
        if (issue != null) {
            return issue;
        } else
            throw new ServiceException("Issue with description '" + description + "' do not exist");
    }

    public Issue updateDescription(Long issueId, String description) {
        Issue issue = issueRepository.findOne(issueId);
        if (issue != null) {
            if (issue.isActive()) {
                issue.setDescription(description);
                return issueRepository.save(issue);
            } else {
                throw new ServiceException("Could not update "
                        + "description on Issue with issueId '" + issueId + "' since it's inactivate.");
            }
        } else
            throw new ServiceException("Failed to update issue with id '" + issueId + "' since it could not be found in the database");
    }

    public Issue inactivate(Long issueId) {
        Issue issue = issueRepository.findOne(issueId);
        if (issue != null) {
            issue.setActive(false);
            return issueRepository.save(issue);
        } else
            throw new ServiceException("Failed to inactive issue with id '" + issueId + "' since it could not be found in the database");
    }

    public Issue activate(Long issueId) {
        Issue issue = issueRepository.findOne(issueId);
        if (issue != null) {
            issue.setActive(true);
            return issueRepository.save(issue);
        } else
            throw new ServiceException("Failed to activate issue with id '" + issueId + "' since it could not be found in the database");
    }

    public Slice<Issue> getAllByPage(int page, int pageSize) {
        return issueRepository.findAll(new PageRequest(page, pageSize));
    }
}
