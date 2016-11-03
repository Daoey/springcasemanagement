package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Issue getIssueById(Long id) {
        Issue issue = issueRepository.findOne(id);
        if (issue != null) {
            return issue;
        } else
            throw new ServiceException("Issue with id '" + id + "' do not exist");
    }

    public Issue getIssueByDescription(String description) {
        Issue issue = issueRepository.findByDescription(description);
        if (issue != null) {
            return issue;
        } else
            throw new ServiceException("Issue with description '" + description + "' do not exist");
    }

    public Issue updateIssueDescription(Long id, String description) {
        Issue issue = issueRepository.findOne(id);
        if (issue != null) {
            if (issue.isActive()) {
                issue.setDescription(description);
                return issueRepository.save(issue);
            } else {
                throw new ServiceException("Could not update "
                        + "description on Issue with id '" + id + "' since it's inactive.");
            }
        } else
            throw new ServiceException("Issue with id '" + id + "' did not exist.");
    }

    public Issue inactiveIssue(Long id) {
        Issue issue = issueRepository.findOne(id);
        if (issue != null) {
            issue.setActive(false);
            return issueRepository.save(issue);
        } else
            throw new ServiceException("Issue with id '" + id + "' did not exist.");
    }

    public Issue activateIssue(Long id) {
        Issue issue = issueRepository.findOne(id);
        if (issue != null) {
            issue.setActive(true);
            return issueRepository.save(issue);
        } else
            throw new ServiceException("Issue with id '" + id + "' did not exist.");
    }
}
