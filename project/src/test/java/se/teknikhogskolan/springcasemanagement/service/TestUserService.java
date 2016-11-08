package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;

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

    @InjectMocks
    private UserService userService;

    private User user;
    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

    @Before
    public void init() {
        user = new User(1L, "Long enough name", "First", "Last");
    }

    @Test
    public void createUserThatFillsRequirements() {
        userService.create(user.getUserNumber(), user.getUsername(), user.getFirstName(), user.getLastName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void createUserTooShortUsername() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Username too short");
        userService.create(user.getUserNumber(), "Too short", user.getFirstName(), user.getLastName());
    }

    @Test
    public void createUserDataAccessExceptionThrown() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to create user: " + user);
        doThrow(dataAccessException).when(userRepository).save(user);
        userService.create(user.getUserNumber(), user.getUsername(), user.getFirstName(), user.getLastName());
    }

    @Test
    public void getUserByIdCallsCorrectMethod() {
        userService.getById(1L);
        verify(userRepository, times(1)).findOne(1L);
    }

    @Test
    public void getUserByUserNumberCallsCorrectMethod() {
        userService.getByUserNumber(1L);
        verify(userRepository, times(1)).findByUserNumber(1L);
    }

    @Test
    public void updateFirstNameCallsCorrectMethodWithNewFirstName() {
        when(userService.getByUserNumber(1L)).thenReturn(user);
        String newFirstName = "New first name";
        userService.updateFirstName(1L, newFirstName);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newFirstName, capturedUser.getValue().getFirstName());
    }

    @Test
    public void updateFirstNameInactiveUserThrowsException() {
        user.setActive(false);
        when(userService.getByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateFirstName(1L, "Some name");
    }

    @Test
    public void updateLastNameCallsCorrectMethodWithNewLastName() {
        when(userService.getByUserNumber(1L)).thenReturn(user);
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
        userService.activate(1L);
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
        userService.inactivate(1L);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());

        for (WorkItem workItem : capturedUser.getValue().getWorkItems()) {
            assertEquals(Status.UNSTARTED, workItem.getStatus());
        }
        assertEquals(false, capturedUser.getValue().isActive());
    }

    @Test
    public void getAllByTeamIdCallsCorrectMethod() {
        userService.getAllByTeamId(1L);
        verify(userRepository, times(1)).findByTeamId(1L);

    }

    @Test
    public void searchUsersCallsCorrectMethod() {
        String name = "some name";
        userService.search(name, name, name);
        verify(userRepository, times(1)).findByFirstNameContainingAndLastNameContainingAndUsernameContaining(name, name,
                name);
    }
}
