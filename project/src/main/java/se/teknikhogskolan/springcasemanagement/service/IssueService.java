package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;

import java.util.List;

@Service
public class IssueService {

    private final IssueRepository issueRepository;

    @Autowired
    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public Issue saveIssue(Issue issue) {
        return issueRepository.save(issue);
    }

    public Issue updateIssueDescription(Long id, String description) {
        Issue issueToUpdate = issueRepository.findOne(id);
        issueToUpdate.setDescription(description);
        return issueRepository.save(issueToUpdate);
    }

    public Issue inactiveIssue(Long id) {
        Issue issueToUpdate = issueRepository.findOne(id);
        issueToUpdate.setActive(false);
        return issueRepository.save(issueToUpdate);
    }

    public Issue activateIssue(Long id) {
        Issue issueToUpdate = issueRepository.findOne(id);
        issueToUpdate.setActive(true);
        return issueRepository.save(issueToUpdate);
    }
}
