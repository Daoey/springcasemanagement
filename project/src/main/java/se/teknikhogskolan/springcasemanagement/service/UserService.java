package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
    }

    public User create(Long userNumber, String username, String firstName, String lastName) {

        if (!usernameLongEnough(username)) {
            throw new ServiceException("Username too short");
        }
        User user = new User(userNumber, username, firstName, lastName);
        try {
            return userRepository.save(user);
        } catch (DataAccessException e) {
            throw new ServiceException("Could not create user: " + user, e);
        }
    }

    public User getById(Long userId) {
        try {
            return userRepository.findOne(userId);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with id: " + userId, e);
        }
    }

    public User getByUserNumber(Long userNumber) {
        try {
            return userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
    }

    public User updateFirstName(Long userNumber, String firstName) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
        if (user.isActive()) {
            user.setFirstName(firstName);
            try {
                return userRepository.save(user);
            } catch (DataAccessException e) {
                throw new ServiceException("Could not update first name of user: " + user, e);
            }
        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User updateLastName(Long userNumber, String lastName) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
        if (user.isActive()) {
            user.setLastName(lastName);
            try {
                return userRepository.save(user);
            } catch (DataAccessException e) {
                throw new ServiceException("Could not update last name of user: " + user, e);
            }
        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User updateUsername(Long userNumber, String username) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
        if (user.isActive()) {
            if (usernameLongEnough(username)) {
                user.setUsername(username);
                try {
                    return userRepository.save(user);
                } catch (DataAccessException e) {
                    throw new ServiceException("Could not update username of user: " + user, e);
                }
            } else {
                throw new ServiceException("Username too short");
            }
        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User activate(Long userNumber) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
        user.setActive(true);
        // Should never throw exception since only activity is changed
        return userRepository.save(user);
    }

    public User inactivate(Long userNumber) {
        User user;
        try {
            user = userRepository.findByUserNumber(userNumber);
        } catch (DataAccessException e) {
            throw new NoSearchResultException("Could not find user with user number: " + userNumber, e);
        }
        user.getWorkItems().forEach(workItem -> workItem.setStatus(Status.UNSTARTED));
        user.setActive(false);
        // Should never throw exception since only activity and status are
        // changed
        return userRepository.save(user);
    }

    public List<User> getAllByTeamId(Long teamId) {
        // Returns empty list if no users found with teamId
        return userRepository.findByTeamId(teamId);
    }

    public List<User> search(String firstName, String lastName, String username) {
        // Returns empty list if no users found matching search criteria
        return userRepository.findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName,
                username);

    }

    private boolean usernameLongEnough(String username) {
        final int minimumLengthUsername = 10;
        return username.length() >= minimumLengthUsername;
    }
}
