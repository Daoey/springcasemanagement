package se.teknikhogskolan.springcasemanagement.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TestIssueService {

    @Mock
    IssueRepository issueRepository;

    @InjectMocks
    IssueService issueService;
    private Long issueId;
    private Issue issueInDb;

    @Before
    public void setUp() {
        this.issueId = 2L;
        this.issueInDb = new Issue("desc");
    }

    @Test
    public void canGetIssueById() throws ServiceException {
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.getIssueById(issueId);

        verify(issueRepository).findOne(issueId);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenGettingIssueByIdThatDoNotExist() throws Exception {
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.getIssueById(issueId);
    }

    @Test
    public void canGetIssueByDescription() throws ServiceException {
        String desc = "Test";
        when(issueRepository.findByDescription(desc)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.getIssueByDescription(desc);

        verify(issueRepository).findByDescription(desc);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenGettingIssueByDescriptionThatDoNotExist() throws Exception {
        String desc = "Test";
        when(issueRepository.findByDescription(desc)).thenReturn(null);
        issueService.getIssueByDescription(desc);
    }

    @Test
    public void canUpdateIssueDescription() throws ServiceException {
        String newDesc = "New desc";
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.updateIssueDescription(issueId, newDesc);
        verify(issueRepository).save(new Issue(newDesc));
        assertEquals(issueFromDb.getDescription(), newDesc);
    }

    @Test(expected = ServiceException.class)
    public void canNotUpdateIssueDescriptionIfInactive() throws ServiceException {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        issueService.updateIssueDescription(issueId, "test");
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenUpdatingDescriptionOnNonExistingIssue() throws Exception {
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.updateIssueDescription(issueId, "test");
    }

    @Test
    public void canInactiveIssue() throws ServiceException {
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.inactiveIssue(issueId);
        verify(issueRepository).save(issueInDb);
        assertFalse(issueFromDb.isActive());
    }

    @Test(expected = ServiceException.class)
    public void throwsExceptionWhenInactivatingAUserThatDoNotExist() throws Exception {
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.inactiveIssue(issueId);
    }

    @Test
    public void canActiveIssue() throws ServiceException {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.activateIssue(issueId);
        verify(issueRepository).save(issueInDb);
        assertTrue(issueFromDb.isActive());
    }

    @Test(expected = ServiceException.class)
    public void throwsExceptionWhenActivatingAUserThatDoNotExist() throws Exception {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.activateIssue(issueId);
    }

    @Test
    public void canGetAllIssuesByPage() throws Exception {
        issueService.getAllIssuesByPage(0, 6);
        verify(issueRepository).findAllByPage(new PageRequest(0, 6));
    }
}