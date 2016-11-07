package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
        return userRepository.save(user);
    }

    public User getById(Long userId) {
        return userRepository.findOne(userId);
    }

    public User getByUserNumber(Long userNumber) {
        return userRepository.findByUserNumber(userNumber);
    }

    public User updateFirstName(Long userNumber, String firstName) {
        User user = userRepository.findByUserNumber(userNumber);
        if (user.isActive()) {
            user.setFirstName(firstName);
            return userRepository.save(user);
        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User updateLastName(Long userNumber, String lastName) {
        User user = userRepository.findByUserNumber(userNumber);
        if (user.isActive()) {
            user.setLastName(lastName);
            return userRepository.save(user);
        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User updateUsername(Long userNumber, String username) {

        User user = userRepository.findByUserNumber(userNumber);
        if (user.isActive()) {
            if (usernameLongEnough(username)) {
                user.setUsername(username);
                return userRepository.save(user);
            } else {
                throw new ServiceException("Username too short");
            }

        } else {
            throw new ServiceException("User is inactive");
        }
    }

    public User activate(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User inactivate(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.getWorkItems().forEach(workItem -> workItem.setStatus(Status.UNSTARTED));
        user.setActive(false);
        return userRepository.save(user);
    }

    public List<User> getAllByTeamId(Long teamId) {
        return userRepository.findByTeamId(teamId);
    }

    public List<User> search(String firstName, String lastName, String username) {
        return userRepository.findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName,
                username);
    }

    private boolean usernameLongEnough(String username) {
        final int minimumLengthUsername = 10;
        return username.length() >= minimumLengthUsername;
    }
}
