package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public UserService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public User saveUser(User user) {

        if (user.getId() != null) {
            throw new ServiceException("User has already been saved");
        }
        
        if (user.getTeam().getId() == null) {
            teamRepository.save(user.getTeam());
        }

        if (!user.isActive()) {
            throw new ServiceException("Can not save inactive user");
        }

        if (!usernameLongEnough(user.getUsername())) {
            throw new ServiceException("Username too short");
        }

        if (teamIsFull(user.getTeam())) {
            throw new ServiceException("Team is full");
        }

        return userRepository.save(user);
    }

    // TODO Remove later?
    public User deleteUser(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        userRepository.delete(user);
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findOne(id);
    }

    public User getUserByUserNumber(Long userNumber) {
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

    public User activateUser(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User inacctivateUser(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.getWorkItems().forEach(workItem -> workItem.setStatus(Status.UNSTARTED));
        user.setActive(false);
        return userRepository.save(user);
    }

    public List<User> searchUsers(String firstName, String lastName, String username) {
        return userRepository.findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName,
                username);
    }
    
    private boolean usernameLongEnough(String username) {
        int maxTeamSize = 10;
        return username.length() >= maxTeamSize;
    }
    
    private boolean teamIsFull(Team team) {
        int maxTeamSize = 10;
        if (team.getUsers().size() > maxTeamSize) {
            return true;
        }
        return false;
    }


}
