package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public final class TestUserService {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private User mockedUser;
    @Mock
    private Team mockedTeam;

    @InjectMocks
    private UserService userService;

    private Team team;
    private User user;

    @Before
    public void init() {
        team = new Team("Team name");
        user = new User(1L, "Long enough name", "First", "Last", team);
    }

    @Test
    public void saveUserThatFillsRequirements() {
        userService.saveUser(user);
        verify(teamRepository, times(1)).save(team);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void saveUserWithTeamAlreadySet() {
        when(mockedUser.getId()).thenReturn(null);
        when(mockedUser.getUsername()).thenReturn("Long enough username");
        when(mockedUser.isActive()).thenReturn(true);
        when(mockedUser.getTeam()).thenReturn(mockedTeam);
        when(mockedTeam.getId()).thenReturn(1L);
        userService.saveUser(mockedUser);
        verify(teamRepository, never()).save(team);
        verify(userRepository, times(1)).save(mockedUser);
    }

    @Test
    public void saveUserWithIdAlreadySet() {
        when(mockedUser.getId()).thenReturn(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User has already been saved");
        userService.saveUser(mockedUser);
    }

    @Test
    public void saveUserTooShortUserName() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Username too short");
        user.setUsername("Too short");
        userService.saveUser(user);
    }

    @Test
    public void saveUserInactiveUser() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Can not save inactive user");
        user.setActive(false);
        userService.saveUser(user);
    }

    @Test
    public void saveUserTeamIsFull() {

        // Creating a collection of users exceeding space limit
        Collection<User> users = new ArrayList<User>();
        int teamMaxSize = 10;
        for (int i = 0; i < teamMaxSize + 1; i++) {
            users.add(user);
        }

        when(mockedUser.getId()).thenReturn(null);
        when(mockedUser.getUsername()).thenReturn("Long enough username");
        when(mockedUser.isActive()).thenReturn(true);
        when(mockedUser.getTeam()).thenReturn(mockedTeam);
        when(mockedTeam.getId()).thenReturn(1L);
        when(mockedTeam.getUsers()).thenReturn(users);

        thrown.expect(ServiceException.class);
        thrown.expectMessage("Team is full");

        userService.saveUser(mockedUser);
    }

    @Test
    public void getUserByIdCallsCorrectMethod() {
        userService.getUserById(1L);
        verify(userRepository, times(1)).findOne(1L);
    }

    @Test
    public void getUserByUserNumberCallsCorrectMethod() {
        userService.getUserByUserNumber(1L);
        verify(userRepository, times(1)).findByUserNumber(1L);
    }

    @Test
    public void updateFirstNameCallsCorrectMethodWithNewFirstName() {
        when(userService.getUserByUserNumber(1L)).thenReturn(user);
        String newFirstName = "New first name";
        userService.updateFirstName(1L, newFirstName);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newFirstName, capturedUser.getValue().getFirstName());
    }

    @Test
    public void updateFirstNameInactiveUserThrowsException() {
        user.setActive(false);
        when(userService.getUserByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateFirstName(1L, "Some name");
    }

    @Test
    public void updateLastNameCallsCorrectMethodWithNewLastName() {
        when(userService.getUserByUserNumber(1L)).thenReturn(user);
        String newLastName = "New last name";
        userService.updateLastName(1L, newLastName);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newLastName, capturedUser.getValue().getLastName());
    }

    @Test
    public void updateLastNameInactiveUserThrowsException() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateLastName(1L, "Some name");
    }

    @Test
    public void updateUsernameCallsCorrectMethodWithNewUsername() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        String newUsername = "New user name";
        userService.updateUsername(1L, newUsername);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newUsername, capturedUser.getValue().getUsername());
    }

    @Test
    public void updateUsernameInactiveUserThrowsException() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateUsername(1L, "Some long enough name");
    }

    @Test
    public void updateUsernameTooShortUsernameThrowsException() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Username too short");
        userService.updateUsername(1L, "Too short");
    }

    @Test
    public void activateUserCallsCorrectMethod() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        userService.activateUser(1L);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(true, capturedUser.getValue().isActive());
    }

    @Test
    public void inactivateUserSetsAllWorkItemsToUnstartedAndInactivatesUser() {
        // Populate a list with some Started work items
        List<WorkItem> workItems = new ArrayList<WorkItem>();
        for (int i = 0; i < 4; i++) {
            WorkItem workItem = new WorkItem("Some workItem");
            workItem.setStatus(Status.STARTED);
            workItems.add(workItem);
        }

        when(userRepository.findByUserNumber(1L)).thenReturn(mockedUser);
        when(mockedUser.getWorkItems()).thenReturn(workItems);
        userService.inacctivateUser(1L);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());

        for (WorkItem workItem : capturedUser.getValue().getWorkItems()) {
            assertEquals(Status.UNSTARTED, workItem.getStatus());
        }
        assertEquals(false, capturedUser.getValue().isActive());
    }

    @Test
    public void searchUsersCallsCorrectMethod() {
        String name = "some name";
        userService.searchUsers(name, name, name);
        verify(userRepository, times(1)).findByFirstNameContainingAndLastNameContainingAndUsernameContaining(name, name,
                name);
    }
}
