package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;
import se.teknikhogskolan.springcasemanagement.service.exception.ServiceException;

@RunWith(MockitoJUnitRunner.class)
public final class TestUserService {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private UserRepository userRepository;

    @Mock
    private User mockedUser;

    @InjectMocks
    private UserService userService;

    private User user;
    private List<User> users;
    private final DataAccessException dataAccessException = new RecoverableDataAccessException("Exception");

    @Before
    public void init() {
        user = new User(1L, "Long enough name", "First", "Last");
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(user);
        }
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
    public void createUserThrowsDataAccessException() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to create user: " + user);
        doThrow(dataAccessException).when(userRepository).save(user);
        userService.create(user.getUserNumber(), user.getUsername(), user.getFirstName(), user.getLastName());
    }

    @Test
    public void getUserByIdReturnsCorrectUser() {
        when(userRepository.findOne(1L)).thenReturn(user);
        User userFromDatabase = userService.getById(1L);
        assertEquals(user, userFromDatabase);
    }

    @Test
    public void getUserByIdThrowsServiceExceptionIfDataAccessException() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with id: 1");
        doThrow(dataAccessException).when(userRepository).findOne(1L);
        userService.getById(1L);
    }

    @Test
    public void getUserByIdThrowsNoSearchResultExceptionIfNoUserFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with id: 1 found");
        when(userRepository.findOne(1L)).thenReturn(null);
        userService.getById(1L);
    }

    @Test
    public void getUserByUserNumberReturnsCorrectUser() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        User userFromDatabase = userService.getByUserNumber(1L);
        assertEquals(user, userFromDatabase);
    }

    @Test
    public void getUserByNumberThrowsServiceExceptionIfDataAccessException() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        userService.getByUserNumber(1L);
    }

    @Test
    public void getUserByNumberThrowsNoSearchResultExceptionIfNoUserFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        userService.getByUserNumber(1L);
    }

    @Test
    public void updateFirstNameCallsCorrectMethodWithNewFirstName() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        String newFirstName = "New first name";
        userService.updateFirstName(1L, newFirstName);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newFirstName, capturedUser.getValue().getFirstName());
    }

    @Test
    public void updateFirstNameInactiveUserThrowsServiceException() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateFirstName(1L, "Some name");
    }

    @Test
    public void updateFirstNameThrowsNoSearchResultExceptionIfNoUserFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        userService.updateFirstName(1L, "some name");
    }

    @Test
    public void updateFirstNameThrowsServiceExceptionIfExceptionIsThrown() {
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        userService.updateFirstName(1L, "some name");
    }

    @Test
    public void updateLastNameCallsCorrectMethodWithNewLastName() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        String newLastName = "New last name";
        userService.updateLastName(1L, newLastName);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(newLastName, capturedUser.getValue().getLastName());
    }

    @Test
    public void updateLastNameInactiveUserThrowsServiceException() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateLastName(1L, "Some name");
    }

    @Test
    public void updateLastNameThrowsNoSearchResultIfNoUserFound() {
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        userService.updateLastName(1L, "some name");
    }

    @Test
    public void updateLastNameThrowsServiceExceptionIfExceptionIsThrown() {
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        userService.updateLastName(1L, "some name");
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
    public void updateUsernameInactiveUserThrowsServiceException() {
        user.setActive(false);
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("User is inactive");
        userService.updateUsername(1L, "Some long enough name");
    }

    @Test
    public void updateUsernameTooShortUsernameThrowsServiceException() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Username too short");
        userService.updateUsername(1L, "Too short");
    }

    @Test
    public void updateUsernameThrowsNoSearchResultExceptionIfNoUserFound() {
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        userService.updateUsername(1L, "some long enough name");
    }

    @Test
    public void updateUsernameThrowsServiceExceptionIfExceptionIsThrown() {
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        userService.updateUsername(1L, "some long enough name");
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
    public void activateUserThrowsNoSearchResultIfNoUserFound() {
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        userService.activate(1L);
    }

    @Test
    public void activateUserThrowsServiceExcptionIfExceptionIsThrown() {
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        userService.activate(1L);
    }

    @Test
    public void inactivateUserSetsAllWorkItemsToUnstartedAndInactivatesUser() {
        // Populate a list with some Started work items
        List<WorkItem> workItems = new ArrayList<>();
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
    public void inactivateUserNoWorkItemsAttachedStillInactivatesUser() {
        when(userRepository.findByUserNumber(1L)).thenReturn(user);
        userService.inactivate(1L);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(capturedUser.capture());
        assertEquals(false, capturedUser.getValue().isActive());
    }

    @Test
    public void inactivateUserThrowsNoSearchResultExceptionIfNoUserFound() {
        when(userRepository.findByUserNumber(1L)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No user with user number: 1 found");
        userService.inactivate(1L);
    }

    @Test
    public void inactivateUserThrowsServiceExceptionIfExceptionIsThrown() {
        doThrow(dataAccessException).when(userRepository).findByUserNumber(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get user with user number: 1");
        userService.inactivate(1L);
    }

    @Test
    public void getAllByTeamIdCallsCorrectMethod() {
        when(userRepository.findByTeamId(1L)).thenReturn(users);
        List<User> usersFromDatabase = userService.getAllByTeamId(1L);
        verify(userRepository, times(1)).findByTeamId(1L);
        assertEquals(users, usersFromDatabase);
    }

    @Test
    public void getAllByTeamIdThrowsNoSearchResultExceptionIfEmptyListReturned() {
        when(userRepository.findByTeamId(1L)).thenReturn(new ArrayList<>());
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users with team id: 1 found");
        userService.getAllByTeamId(1L);
    }

    @Test
    public void getAllByTeamIdThrowsNoSearchResultExceptionIfNullReturned() {
        when(userRepository.findByTeamId(1L)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users with team id: 1 found");
        userService.getAllByTeamId(1L);
    }

    @Test
    public void getAllByTeamIdThrowsServiceExceptionIfExceptionThrown() {
        doThrow(dataAccessException).when(userRepository).findByTeamId(1L);
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get all users with team id: 1");
        userService.getAllByTeamId(1L);
    }

    @Test
    public void searchUsersCallsCorrectMethod() {
        String name = "some name";
        when(userRepository.searchUsers(name, name, name))
                .thenReturn(users);
        List<User> usersFromDatabase = userService.search(name, name, name);
        verify(userRepository, times(1)).searchUsers(name, name,
                name);
        assertEquals(users, usersFromDatabase);
    }

    @Test
    public void searchUsersThrowsNoSearchResultExceptionIfEmptyListReturned() {
        when(userRepository.searchUsers("first", "last",
                "user")).thenReturn(new ArrayList<>());
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users fulfilling criteria: firstName = first, lastName = last, username = user");
        userService.search("first", "last", "user");
    }

    @Test
    public void searchUsersThrowsNoSearchResultExceptionIfNullReturned() {
        when(userRepository.searchUsers("first", "last",
                "user")).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users fulfilling criteria: firstName = first, lastName = last, username = user");
        userService.search("first", "last", "user");
    }

    @Test
    public void searchUsersThrowsServiceExceptionIfExceptionIsThrown() {
        doThrow(dataAccessException).when(userRepository)
                .searchUsers("first", "last", "user");
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get users with criteria: firstName = first, lastName = last, username = user");
        userService.search("first", "last", "user");
    }

    @Test
    public void getAllByPageCallsCorrectMethodAndReturnsCorrectSliceOfUsers() {
        Page<User> pageUsers = new PageImpl<>(users);
        PageRequest pageRequest = new PageRequest(0, 10);
        when(userRepository.findAll(pageRequest)).thenReturn(pageUsers);
        Slice<User> pageUsersFromDatabase = userService.getAllByPage(0, 10);
        assertEquals(pageUsers, pageUsersFromDatabase);
    }

    @Test
    public void getAllByPageThrowsServiceExceptionIfExceptionThrown() {
        doThrow(dataAccessException).when(userRepository).findAll(new PageRequest(4, 10));
        thrown.expect(ServiceException.class);
        thrown.expectMessage("Failed to get users by page");
        userService.getAllByPage(4, 10);
    }

    @Test
    public void getAllByPageThrowsNoSearchResultExceptionIfNoUsersFound() {
        when(userRepository.findAll(new PageRequest(4, 10))).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users on page: 4");
        userService.getAllByPage(4, 10);
    }

    @Test
    public void getByCreationDateCallsCorrectMethodAndReturnsCorrectUsers() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        when(userRepository.findByCreationDate(startDate, endDate)).thenReturn(users);
        List<User> usersReturned = userService.getByCreationDate(startDate, endDate);
        verify(userRepository, times(1)).findByCreationDate(startDate, endDate);
        assertEquals(users, usersReturned);
    }

    @Test
    public void getByCreationDateThrowsServiceExceptionIfExceptionThrown() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);
        doThrow(dataAccessException).when(userRepository).findByCreationDate(startDate, endDate);
        thrown.equals(ServiceException.class);
        thrown.expectMessage("Failed to get users created between: " + startDate + " and " + endDate);
        userService.getByCreationDate(startDate, endDate);
    }

    @Test
    public void getByCreationDateThrowsNoSearchResultExceptionIfNullIsReturned() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);
        when(userRepository.findByCreationDate(startDate, endDate)).thenReturn(null);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users created between: " + startDate + " and " + endDate);
        userService.getByCreationDate(startDate, endDate);
    }

    @Test
    public void getByCreationDateThrowsNoSearchResultExceptionIfEmptyListIsReturned() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(3);
        when(userRepository.findByCreationDate(startDate, endDate)).thenReturn(new ArrayList<>());
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("No users created between: " + startDate + " and " + endDate);
        userService.getByCreationDate(startDate, endDate);
    }
}