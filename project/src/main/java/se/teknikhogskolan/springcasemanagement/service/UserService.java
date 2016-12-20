package se.teknikhogskolan.springcasemanagement.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotAllowedException;
import se.teknikhogskolan.springcasemanagement.service.exception.InvalidInputException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(Long userNumber, String username, String firstName, String lastName) {
        User user;
        if (usernameLongEnough(username)) {
            user = new User(userNumber, username, firstName, lastName);
        } else {
            throw new InvalidInputException("Username too short");
        }
        return saveUser(user, "Failed to create user: " + user);
    }

    public User getById(Long userId) {
        User user;
        try {
            user = userRepository.findOne(userId);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get user with id: " + userId, e);
        }
        if (user == null) {
            throw new NotFoundException("No user with id: " + userId + " found");
        } else {
            return user;
        }
    }

    public User getByUserNumber(Long userNumber) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get user with user number: " + userNumber, e);
        }
        if (user == null) {
            throw new NotFoundException("No user with user number: " + userNumber + " found");
        } else {
            return user;
        }
    }

    public User updateFirstName(Long userNumber, String firstName) {
        User user = getByUserNumber(userNumber);
        if (user.isActive()) {
            user.setFirstName(firstName);
            return saveUser(user, "Failed to update firstName on user with user number: " + userNumber);
        } else {
            throw new NotAllowedException("User is inactive");
        }
    }

    public User updateLastName(Long userNumber, String lastName) {
        User user = getByUserNumber(userNumber);
        if (user.isActive()) {
            user.setLastName(lastName);
            return saveUser(user, "Failed to update lastName on user with user number: " + userNumber);
        } else {
            throw new NotAllowedException("User is inactive");
        }
    }

    public User updateUsername(Long userNumber, String username) {
        User user = getByUserNumber(userNumber);
        if (user.isActive()) {
            if (usernameLongEnough(username)) {
                user.setUsername(username);
                return saveUser(user, "Failed to update username on user with user number: " + userNumber);
            } else {
                throw new InvalidInputException("Username too short");
            }
        } else {
            throw new NotAllowedException("User is inactive");
        }
    }

    public User activate(Long userNumber) {
        User user = getByUserNumber(userNumber);
        user.setActive(true);
        return saveUser(user, "Failed to activate user with user number: " + userNumber);
    }

    public User inactivate(Long userNumber) {

        User user = getByUserNumber(userNumber);
        if (user.getWorkItems() != null) {
            user.getWorkItems().forEach(workItem -> workItem.setStatus(Status.UNSTARTED));
        }
        user.setActive(false);
        return saveUser(user, "Failed to inactivate user with user number: " + userNumber);
    }

    public List<User> getAllByTeamId(Long teamId) {
        List<User> users;
        try {
            users = userRepository.findByTeamId(teamId);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get all users with team id: " + teamId, e);
        }
        if (users == null || users.size() == 0) {
            throw new NotFoundException("No users with team id: " + teamId + " found");
        } else {
            return users;
        }
    }

    public List<User> search(String firstName, String lastName, String username) {
        List<User> users;
        try {
            users = userRepository.searchUsers(firstName, lastName, username);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get users with criteria: firstName = " + firstName
                    + ", lastName = " + lastName + ", username = " + username, e);
        }
        if (users == null || users.size() == 0) {
            throw new NotFoundException("No users fulfilling criteria: " + "firstName = " + firstName
                    + ", lastName = " + lastName + ", username = " + username);
        } else {
            return users;
        }
    }

    public Page<User> getAllByPage(int pageNumber, int pageSize) {
        Page<User> page;
        try {
            page = userRepository.findAll(new PageRequest(pageNumber, pageSize));
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get users by page", e);
        }
        if (page != null) {
            return page;
        } else {
            throw new NotFoundException("No users on page: " + pageNumber);
        }
    }

    public List<User> getByCreationDate(LocalDate startDate, LocalDate endDate) {
        
        List<User> users;
        try {
            users = userRepository.findByCreationDate(startDate, endDate);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to get users created between: " + startDate + " and " + endDate,
                    e);
        }
        
        if (users == null || users.size() == 0) {
            throw new NotFoundException("No users created between: " + startDate + " and " + endDate);
        } else {
            return users;
        }
    }

    private User saveUser(User user, String dataConnectivityExceptionMessage) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new DatabaseException(dataConnectivityExceptionMessage, e);
        }
    }

    private boolean usernameLongEnough(String username) {
        final int minimumLengthUsername = 10;
        return username.length() >= minimumLengthUsername;
    }
}