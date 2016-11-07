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
        Issue issueFromDb = issueService.getById(issueId);

        verify(issueRepository).findOne(issueId);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenGettingIssueByIdThatDoNotExist() throws Exception {
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.getById(issueId);
    }

    @Test
    public void canGetIssueByDescription() throws ServiceException {
        String desc = "Test";
        when(issueRepository.findByDescription(desc)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.getByDescription(desc);

        verify(issueRepository).findByDescription(desc);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenGettingIssueByDescriptionThatDoNotExist() throws Exception {
        String desc = "Test";
        when(issueRepository.findByDescription(desc)).thenReturn(null);
        issueService.getByDescription(desc);
    }

    @Test
    public void canUpdateIssueDescription() throws ServiceException {
        String newDesc = "New desc";
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.updateDescription(issueId, newDesc);
        verify(issueRepository).save(new Issue(newDesc));
        assertEquals(issueFromDb.getDescription(), newDesc);
    }

    @Test(expected = ServiceException.class)
    public void canNotUpdateIssueDescriptionIfInactive() throws ServiceException {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        issueService.updateDescription(issueId, "test");
    }

    @Test(expected = ServiceException.class)
    public void shouldThrowExceptionWhenUpdatingDescriptionOnNonExistingIssue() throws Exception {
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void canInactiveIssue() throws ServiceException {
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.inactivate(issueId);
        verify(issueRepository).save(issueInDb);
        assertFalse(issueFromDb.isActive());
    }

    @Test(expected = ServiceException.class)
    public void throwsExceptionWhenInactivatingAUserThatDoNotExist() throws Exception {
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.inactivate(issueId);
    }

    @Test
    public void canActiveIssue() throws ServiceException {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.activate(issueId);
        verify(issueRepository).save(issueInDb);
        assertTrue(issueFromDb.isActive());
    }

    @Test(expected = ServiceException.class)
    public void throwsExceptionWhenActivatingAUserThatDoNotExist() throws Exception {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.activate(issueId);
    }

    @Test
    public void canGetAllIssuesByPage() throws Exception {
        issueService.getAllByPage(0, 6);
        verify(issueRepository).findAll(new PageRequest(0, 6));
    }
}