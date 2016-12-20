package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import se.teknikhogskolan.springcasemanagement.repository.paging.PagingIssueRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;
import se.teknikhogskolan.springcasemanagement.service.exception.ServiceException;

@RunWith(MockitoJUnitRunner.class)
public final class TestIssueService {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private PagingIssueRepository pagingIssueRepository;

    @InjectMocks
    private IssueService issueService;
    private Long issueId;
    private Issue issueInDb;

    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

    @Before
    public void setUp() {
        this.issueId = 2L;
        this.issueInDb = new Issue("desc");
    }

    @Test
    public void canGetIssueById() {
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.getById(issueId);
        verify(issueRepository).findOne(issueId);
        assertEquals(issueFromDb, issueInDb);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingIssueByIdThatDoNotExist() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Issue with id '" + issueId + "' do not exist");
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.getById(issueId);
    }

    @Test
    public void shouldThrowDatabaseExceptionIfErrorOccursWhenGettingIssueById() {
        thrown.expect(DatabaseException.class);
        thrown.expectMessage("Could not find issue with id: " + issueId);
        doThrow(dataAccessException).when(issueRepository).findOne(issueId);
        issueService.getById(issueId);
    }

    @Test
    public void canGetIssueByDescription() {
        String desc = "Test";
        List<Issue> issuesInDb = new ArrayList<>();
        issuesInDb.add(issueInDb);
        when(issueRepository.findByDescription(desc)).thenReturn(issuesInDb);
        List<Issue> issuesFromDb = issueService.getByDescription(desc);

        verify(issueRepository).findByDescription(desc);
        assertEquals(issuesFromDb.get(0), issueInDb);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingIssueByDescriptionThatDoNotExist() {
        String desc = "Test";
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Issues with description '" + desc + "' do not exist");
        when(issueRepository.findByDescription(desc)).thenReturn(null);
        issueService.getByDescription(desc);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenGettingIssueByDescription() {
        String desc = "Test";
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get issues with description: " + desc);
        doThrow(dataAccessException).when(issueRepository).findByDescription(desc);
        issueService.getByDescription(desc);
    }

    @Test
    public void canUpdateIssueDescription() {
        String newDesc = "New desc";
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.updateDescription(issueId, newDesc);
        verify(issueRepository).save(new Issue(newDesc));
        assertEquals(issueFromDb.getDescription(), newDesc);
    }

    @Test
    public void shouldThrowServiceExceptionIfIssueIsInactiveWhenUpdatingIssueDescription() {
        issueInDb.setActive(false);
        thrown.expect(ServiceException.class);
        thrown.expectMessage(
                "Could not update " + "description on Issue with id '" + issueId + "' since it's inactivate.");
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenUpdatingDescriptionOnANonExistingIssue() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(
                "Failed to update issue with id '" + issueId + "' since it could not be found in the database");
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void shouldThrowDatabaseExceptionIfErrorOccursWhenUpdatingIssueDescription() {
        thrown.expect(DatabaseException.class);
        thrown.expectMessage("Could not update description on issue with id: " + issueId);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        doThrow(dataAccessException).when(issueRepository).save(issueInDb);
        issueService.updateDescription(issueId, "test");
    }

    @Test
    public void canInactiveIssue() {
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.inactivate(issueId);
        verify(issueRepository).save(issueInDb);
        assertFalse(issueFromDb.isActive());
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenInactivatingAnIssueThatDoNotExist() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(
                "Failed to inactivate issue with id '" + issueId + "' since it could not be found in the database");
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.inactivate(issueId);
    }

    @Test
    public void shouldThrowDatabaseExceptionIfErrorOccursWhenInactivatingAnIssue() {
        thrown.expect(DatabaseException.class);
        thrown.expectMessage("Could not inactivate issue with id: " + issueId);
        issueInDb.setActive(true);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        doThrow(dataAccessException).when(issueRepository).save(issueInDb);
        issueService.inactivate(issueId);
    }

    @Test
    public void canActiveIssue() {
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        when(issueRepository.save(issueInDb)).thenReturn(issueInDb);
        Issue issueFromDb = issueService.activate(issueId);
        verify(issueRepository).save(issueInDb);
        assertTrue(issueFromDb.isActive());
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenActivatingAnIssueThatDoNotExist() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(
                "Failed to activate issue with id '" + issueId + "' since it could not be found in the database");
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(null);
        issueService.activate(issueId);
    }

    @Test
    public void shouldThrowDatabaseExceptionIfErrorOccursWhenActivatingAnIssue() {
        thrown.expect(DatabaseException.class);
        thrown.expectMessage("Could not activate issue with id: " + issueId);
        issueInDb.setActive(false);
        when(issueRepository.findOne(issueId)).thenReturn(issueInDb);
        doThrow(dataAccessException).when(issueRepository).save(issueInDb);
        issueService.activate(issueId);
    }

    @Test
    public void shouldThrowExceptionIfPageIsEmpty() {
        thrown.expect(NotFoundException.class);
        issueService.getAllByPage(0, 6);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccurredWhenGettingPage() {
        doThrow(dataAccessException).when(pagingIssueRepository).findAll(new PageRequest(0, 6));
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get issues by page");
        issueService.getAllByPage(0, 6);
    }
}