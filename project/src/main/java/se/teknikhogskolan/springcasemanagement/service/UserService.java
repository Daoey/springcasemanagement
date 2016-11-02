package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
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

        // TODO teamRepository.saveTeam(user.getTeam());

        if (!user.isActive()) {
            throw new RuntimeException("Can not save inactive user");
        }

        if (user.getId() != null) {
            throw new RuntimeException("User has already been saved.");
        }

        if (!usernameLongEnough(user.getUsername())) {
            throw new RuntimeException("Username too short.");
        }

        if (teamIsFull(user.getTeam())) {
            throw new RuntimeException("Team is full.");
        }

        return userRepository.save(user);
    }

    private boolean teamIsFull(Team team) {
        // TODO Implement me
        return false;
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
            throw new RuntimeException("User is inactive");
        }
    }

    public User updateLastName(Long userNumber, String lastName) {
        User user = userRepository.findByUserNumber(userNumber);
        if (user.isActive()) {
            user.setLastName(lastName);
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User is inactive");
        }
    }

    public User updateUsername(Long userNumber, String username) {

        User user = userRepository.findByUserNumber(userNumber);
        if (user.isActive()) {
            if (usernameLongEnough(username)) {
                user.setUsername(username);
                return userRepository.save(user);
            } else {
                throw new RuntimeException("Username too short.");
            }

        } else {
            throw new RuntimeException("User is inactive");
        }
    }

    public User activateUser(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User inacctivateUser(Long userNumber) {
        User user = userRepository.findByUserNumber(userNumber);
        user.setActive(false);
        return userRepository.save(user);
    }

    public List<User> searchUsers(String firstName, String lastName, String username) {
        return userRepository.findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName,
                username);
    }

    // private boolean userFillsRequirements(User user) throws
    // RepositoryException {
    // if (!usernameLongEnough(user.getUsername())) {
    // return false;
    // }
    // if (!teamHasSpaceForUser(user.getTeamId(), user.getId())) {
    // return false;
    // }
    // return true;
    // }
    //
    private boolean usernameLongEnough(String username) {
        int maxTeamSize = 10;
        return username.length() >= maxTeamSize;
    }
    //
    // private boolean teamHasSpaceForUser(int teamId, int userId) throws
    // RepositoryException {
    //
    // if (teamId == 0) {
    // return true;
    // }
    // return numberOfUsersInTeamLessThanTen(teamId);
    // }
    //
    // private boolean numberOfUsersInTeamLessThanTen(int teamId) throws
    // RepositoryException {
    // List<User> users = userRepository.getUsersByTeamId(teamId);
    // return users.size() < 10;
    // }
    //
    // private void setStatusOfAllWorkItemsOfUserToUnstarted(int userId) throws
    // RepositoryException {
    //
    // List<WorkItem> workItems =
    // workItemRepository.getWorkItemsByUserId(userId);
    // for (WorkItem workItem : workItems) {
    // workItemRepository.updateStatusById(workItem.getId(),
    // WorkItem.Status.UNSTARTED);
    // }
    // }

}
