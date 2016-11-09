package se.teknikhogskolan.springcasemanagement.service;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@RunWith(MockitoJUnitRunner.class)
public final class TestTeamService {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

    private Team teamInDb;
    private Team team;
    private User user;
    private Long teamId;
    private Long userId;

    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

    @Before
    public void setUp() {
        this.teamInDb = new Team("Team in db");
        this.team = new Team("Team");
        this.user = new User(4L, "test", "test", "test").setTeam(team);
        this.teamId = 5L;
        this.userId = 1L;
    }

    @Test
    public void canGetTeamById() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        teamService.getById(teamId);
        verify(teamRepository).findOne(teamId);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingTeamByIdThatDoNotExist() throws NoSearchResultException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' do not exist");
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamService.getById(teamId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursGettingTeamById() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get team with id: " + teamId);
        doThrow(dataAccessException).when(teamRepository).findOne(teamId);
        teamService.getById(teamId);
    }

    @Test
    public void canGetTeamByName() throws ServiceException {
        String name = teamInDb.getName();
        when(teamRepository.findByName(name)).thenReturn(teamInDb);
        teamService.getByName(name);
        verify(teamRepository).findByName(name);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenGettingTeamByNameThatDoNotExist() throws NoSearchResultException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with name '" + team.getName() + "' do not exist");
        when(teamRepository.findByName(team.getName())).thenReturn(null);
        teamService.getByName(team.getName());
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursGettingTeamByName() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get team with name: " + team.getName());
        doThrow(dataAccessException).when(teamRepository).findByName(team.getName());
        teamService.getByName(team.getName());
    }

    @Test
    public void canCreateTeam() throws ServiceException {
        teamService.create(team.getName());
        verify(teamRepository).save(team);
    }

    @Test
    public void shouldThrowServiceExceptionWhenCreatingTeamWithAnAlreadyExistingName() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Team wit name '" + team.getName() + "' already exist");
        doThrow(DuplicateKeyException.class).when(teamRepository).save(team);
        teamService.create(team.getName());
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenCreatingTeam() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not create team with name: " + team.getName());
        doThrow(dataAccessException).when(teamRepository).save(team);
        teamService.create(team.getName());
    }

    @Test
    public void canUpdateTeamName() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        when(teamRepository.save(teamInDb)).thenReturn(teamInDb);
        teamInDb.setName("Old name");
        String newName = "New name";
        Team team = teamService.updateName(teamId, newName);

        verify(teamRepository).save(teamInDb);
        assertEquals(team.getName(), newName);
    }

    @Test
    public void shouldThrowServiceExceptionIfTeamIsInactiveWhenUpdatingName() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not update "
                + "name on team with id '" + teamId + "' since it's inactive.");
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        teamInDb.setActive(false);
        teamService.updateName(teamId, newName);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenUpdatingNameOnANonExistingTeam() throws Exception {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' do not exist.");
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamInDb.setActive(false);
        teamService.updateName(teamId, newName);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenUpdatingTeamName() throws Exception {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not update name on team with id: " + teamId);
        String newName = "New name";
        doThrow(dataAccessException).when(teamRepository).findOne(teamId);
        teamInDb.setActive(false);
        teamService.updateName(teamId, newName);
    }

    @Test
    public void canInactivateTeam() throws ServiceException {
        teamInDb.setActive(true);
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        when(teamRepository.save(teamInDb)).thenReturn(teamInDb);
        Team teamFromDb = teamService.inactive(teamId);
        verify(teamRepository).save(teamInDb);
        assertFalse(teamFromDb.isActive());

    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenTryingToInactivateANonExistingTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Failed to inactive team with id '"
                + teamId + "' since it could not be found in the database");
        teamInDb.setActive(true);
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamService.inactive(teamId);
    }

    @Test
    public void shouldThrowServiceExceptionIfAnErrorOccursWhenInactivatingATeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not inactive team with id: " + teamId);
        teamInDb.setActive(true);
        doThrow(dataAccessException).when(teamRepository).findOne(teamId);
        teamService.inactive(teamId);
    }

    @Test
    public void canActivateTeam() throws ServiceException {
        teamInDb.setActive(false);
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        when(teamRepository.save(teamInDb)).thenReturn(teamInDb);
        Team teamFromDb = teamService.activate(teamId);
        verify(teamRepository).save(teamInDb);
        assertTrue(teamFromDb.isActive());
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenTryingToActivateANonExistingTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Failed to activate team with id '"
                + teamId + "' since it could not be found in the database");
        teamInDb.setActive(false);
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamService.activate(teamId);
    }

    @Test
    public void shouldThrowServiceExceptionIfAnErrorOccursWhenActivatingATeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not activate team with id: " + teamId);
        teamInDb.setActive(false);
        doThrow(dataAccessException).when(teamRepository).findOne(teamId);
        teamService.activate(teamId);
    }

    @Test
    public void canGetAllTeams() throws ServiceException {
        when(teamRepository.findAll()).thenReturn(new ArrayList<>());
        teamService.getAll();
        verify(teamRepository).findAll();
    }

    @Test
    public void shouldThrowNoSearchResultExceptionWhenThereAreNoTeams() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("No teams were found in the database");
        when(teamRepository.findAll()).thenReturn(null);
        teamService.getAll();
    }

    @Test
    public void shouldThrowServiceExceptionIfAnErrorOccursWhenGettingAllTeams() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not get all teams");
        doThrow(dataAccessException).when(teamRepository).findAll();
        teamService.getAll();
    }


    @Test
    public void canAddUserToTeam() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.addUserToTeam(teamId, userId);
        verify(userRepository).save(user);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionIfUserIdIsNullWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(null);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionIfTeamIdIsNullWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(null);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void shouldThrowServiceExceptionIfUserIsInactiveWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        user.setActive(false);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void shouldThrowServiceExceptionIfTeamIsInactiveWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        team.setActive(false);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not add user with id '" + userId
                + "' to team with id '" + teamId);
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        doThrow(dataAccessException).when(userRepository).save(user);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void canDeleteUserFromTeam() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.removeUserFromTeam(teamId, userId);
        verify(userRepository).save(user);
        assertNull(user.getTeam());
    }

    @Test
    public void shouldThrowNoSearchResultExceptionIfUserIdIsNullWhenRemovingUserToTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(null);
        teamService.removeUserFromTeam(teamId, userId);
    }

    @Test
    public void shouldThrowNoSearchResultExceptionIfTeamIdIsNullWhenRemovingUserToTeam() throws ServiceException {
        thrown.expect(NoSearchResultException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(null);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.removeUserFromTeam(teamId, userId);
    }

    @Test
    public void throwServiceExceptionIfUserIsInactiveWhenRemovingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        user.setActive(false);
        teamService.removeUserFromTeam(teamId, userId);
    }

    @Test
    public void shouldThrowServiceExceptionIfTeamIsInactiveWhenRemovingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        team.setActive(false);
        teamService.removeUserFromTeam(teamId, userId);
    }

    @Test
    public void shouldThrowServiceExceptionIfErrorOccursWhenRemovingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Could not remove user with id '" + userId
                + "' from team with id '" + teamId);
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        doThrow(dataAccessException).when(userRepository).save(user);
        teamService.removeUserFromTeam(teamId, userId);
    }
}