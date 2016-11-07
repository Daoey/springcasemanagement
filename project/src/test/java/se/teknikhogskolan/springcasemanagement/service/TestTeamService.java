package se.teknikhogskolan.springcasemanagement.service;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.repository.IssueRepository;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private Team teamInDb;
    private Team team;
    private User user;
    private Long teamId;
    private Long userId;

    @Before
    public void setUp() {
        this.teamInDb = new Team("Team in db");
        this.team = new Team("Team");
        this.user = new User(4L, "test", "test", "test", team);
        this.teamId = 5L;
        this.userId = 1L;
    }

    @Test
    public void canGetTeamByName() throws ServiceException {
        String name = teamInDb.getName();
        when(teamRepository.findByName(name)).thenReturn(teamInDb);
        teamService.getByName(name);
        verify(teamRepository).findByName(name);
    }

    @Test(expected = ServiceException.class)
    public void canNotGetTeamByNameIfTeamDoNotExist() throws ServiceException {
        when(teamRepository.findByName(team.getName())).thenReturn(null);
        teamService.getByName(team.getName());
    }

    @Test
    public void canGetTeamById() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        teamService.getById(teamId);
        verify(teamRepository).findOne(teamId);
    }

    @Test(expected = ServiceException.class)
    public void canNotGetTeamByIdIfTeamDoNotExist() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamService.getById(teamId);
    }

    @Test
    public void canCreateTeam() throws ServiceException {
        teamService.create(team.getName());
        verify(teamRepository).save(team);
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

    @Test(expected = ServiceException.class)
    public void canNotUpdateTeamNameIfTeamIsInactive() throws ServiceException {
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        teamInDb.setActive(false);
        teamService.updateName(teamId, newName);
    }

    @Test(expected = ServiceException.class)
    public void canNotUpdateTeamNameIfTeamDoNotExist() throws ServiceException {
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(null);
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

    @Test(expected = ServiceException.class)
    public void canNotInactiveTeamIfTeamDoNotExist() throws ServiceException {
        teamInDb.setActive(true);
        when(teamRepository.findOne(teamId)).thenReturn(null);
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

    @Test(expected = ServiceException.class)
    public void canNotActiveTeamIfTeamDoNotExist() throws ServiceException {
        teamInDb.setActive(false);
        when(teamRepository.findOne(teamId)).thenReturn(null);
        teamService.activate(teamId);
    }

    @Test
    public void canGetAllTeams() throws ServiceException {
        teamService.getAll();
        verify(teamRepository).findAll();
    }

    @Test
    public void throwExceptionIfUserIdIsNullWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(null);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void throwExceptionIfTeamIdIsNullWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Team with id '" + teamId + "' or User with id '" + userId + "' did not exist.");
        when(teamRepository.findOne(teamId)).thenReturn(null);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void throwExceptionIfUserIsInactiveWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        user.setActive(false);
        teamService.addUserToTeam(teamId, userId);
    }

    @Test
    public void throwExceptionIfTeamIsInactiveWhenAddingUserToTeam() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        team.setActive(false);
        teamService.addUserToTeam(teamId, userId);
    }

   /* @Test
    public void canNotAddUserToTeamIfTeamIsFull() throws ServiceException {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Team with id '" + teamId + "' already contains 10 users");
        int maxAmountOfUsers = 10;
        addUserToDb(maxAmountOfUsers);
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.addUserToTeam(teamId, userId);
    }*/

    @Test
    public void canAddUserToTeam() throws ServiceException {
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.addUserToTeam(teamId, userId);

        verify(userRepository).save(user);
    }

    @Test
    public void canDeleteUserFromTeam() throws Exception {
        when(teamRepository.findOne(teamId)).thenReturn(team);
        when(userRepository.findOne(userId)).thenReturn(user);
        teamService.removeUserFromTeam(teamId, userId);

        verify(userRepository).save(user);
        assertNull(user.getTeam());
    }


    private List<User> addUserToDb(int amount) {
        List<User> usersInDb = new ArrayList<>();
        executeVoid(UserRepository -> {
            for (int i = 0; i < amount; i++) {
                User user = new User(1L + i, "username" + i, "first", "last", team);
                usersInDb.add(user);
                userRepository.save(user);
            }
        });
        return usersInDb;
    }

    private void executeVoid(Consumer<UserRepository> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(projectPackage);
            context.refresh();
            UserRepository userRepository = context.getBean(UserRepository.class);
            operation.accept(userRepository);
        }
    }
}