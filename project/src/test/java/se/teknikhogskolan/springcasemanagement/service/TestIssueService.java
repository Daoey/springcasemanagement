package se.teknikhogskolan.springcasemanagement.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.data.domain.PageRequest;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;


import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TestIssueService {

    @Mock
    IssueRepository issueRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    IssueService issueService;
    private Long issueId;
    private Issue issueInDb;

    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

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

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingIssueByIdThatDoNotExist() throws Exception {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Issue with issueId '" + issueId + "' do not exist");
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.getById(issueId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenGettingIssueById() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get issue with id: " + issueId);
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
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

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingIssueByDescriptionThatDoNotExist() throws Exception {
        String desc = "Test";
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Issue with description '" + desc + "' do not exist");
        when(issueRepository.findByDescription(desc)).thenReturn(null);
        issueService.getByDescription(desc);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenGettingIssueByDescription() throws Exception {
        String desc = "Test";
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get issue with description: " + desc);
        doThrow(dataAccessException).when(issueRepository).findByDescription(desc);
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

    @Test
    public void shouldThrowServiceExceptionIfIssueIsInactiveWhenUpdatingIssueDescription() throws ServiceException {
        issueInDb.setActive(false);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not update "
                + "description on Issue with issueId '" + issueId + "' since it's inactivate.");
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenUpdatingDescriptionOnANonExistingIssue() throws Exception {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Failed to update issue with id '"
                + issueId + "' since it could not be found in the database");
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenUpdatingIssueDescription() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not update description on issue with id: " + issueId);
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
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

    @Test
    public void shouldThrowNoSearchResultExceptionWhenInactivatingAnIssueThatDoNotExist() throws Exception {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Failed to inactive issue with id '"
                + issueId + "' since it could not be found in the database");
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.inactivate(issueId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenInactivatingAnIssue() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not inactive issue with id: " + issueId);
        issueInDb.setActive(true);
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
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

    @Test
    public void shouldThrowNoSearchResultExceptionWhenActivatingAnIssueThatDoNotExist() throws Exception {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Failed to activate issue with id '"
                + issueId + "' since it could not be found in the database");
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.activate(issueId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenActivatingAnIssue() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not active issue with id: " + issueId);
        issueInDb.setActive(false);
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
        issueService.activate(issueId);
    }

    @Test
    public void shouldThrowExceptionIfPageIsEmpty() throws Exception {
        thrown.expect(NoSearchResultException.class);
        issueService.getAllByPage(0, 6);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccurredWhenGettingPage() throws Exception {
        doThrow(dataAccessException).when(issueRepository).findAll(new PageRequest(0, 6));
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get issues by page");
        issueService.getAllByPage(0, 6);
    }
}