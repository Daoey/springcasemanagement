package se.teknikhogskolan.springcasemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(Long userNumber, String username, String firstName, String lastName) {
        if (!usernameLongEnough(username)) {
            throw new ServiceException("Username too short");
        }
        User user = new User(userNumber, username, firstName, lastName);
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new ServiceException("Failed to create user: " + user, e);
        }
    }

    public User getById(Long userId) {
        try {
            User user = userRepository.findOne(userId);
            if (user == null) {
                throw new NoSearchResultException("No user with id: " + userId + " found");
            } else {
                return user;
            }
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get user with id: " + userId, e);
        }
    }

    public User getByUserNumber(Long userNumber) {
        try {
            User user = userRepository.findByUserNumber(userNumber);
            if (user == null) {
                throw new NoSearchResultException("No user with user number: " + userNumber + " found");
            } else {
                return user;
            }
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get user with user number: " + userNumber, e);
        }
    }

    public User updateFirstName(Long userNumber, String firstName) {
        try {
            User user = userRepository.findByUserNumber(userNumber);
            if (user.isActive()) {
                user.setFirstName(firstName);
                return userRepository.save(user);
            } else {
                throw new ServiceException("User is inactive");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("No user with user number: " + userNumber + " found", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to update firstName on user with user number: " + userNumber, e);
        }
    }

    public User updateLastName(Long userNumber, String lastName) {
        try {
            User user = userRepository.findByUserNumber(userNumber);
            if (user.isActive()) {
                user.setLastName(lastName);
                return userRepository.save(user);
            } else {
                throw new ServiceException("User is inactive");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("No user with user number: " + userNumber + " found", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to update lastName on user with user number: " + userNumber, e);
        }
    }

    public User updateUsername(Long userNumber, String username) {
        try {
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
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("No user with user number: " + userNumber + " found", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to update username on user with user number: " + userNumber, e);
        }
    }

    public User activate(Long userNumber) {
        try {
            User user = userRepository.findByUserNumber(userNumber);
            user.setActive(true);
            return userRepository.save(user);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("No user with user number: " + userNumber + " found", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to activate user with user number: " + userNumber, e);
        }
    }

    public User inactivate(Long userNumber) {
        try {
            User user = userRepository.findByUserNumber(userNumber);
            if (user.getWorkItems() != null) {
                user.getWorkItems().forEach(workItem -> workItem.setStatus(Status.UNSTARTED));
            }
            user.setActive(false);
            return userRepository.save(user);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("No user with user number: " + userNumber + " found", e);
        } catch (Exception e) {
            throw new ServiceException("Failed to inactivate user with user number: " + userNumber, e);
        }
    }

    public List<User> getAllByTeamId(Long teamId) {
        try {
            List<User> users = userRepository.findByTeamId(teamId);
            if (users == null || users.size() == 0) {
                throw new NoSearchResultException("No users with team id: " + teamId + " found");
            } else {
                return users;
            }
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get all users with team id: " + teamId, e);
        }
    }

    public List<User> search(String firstName, String lastName, String username) {
        try {
            List<User> users = userRepository
                    .findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName, username);
            if (users == null || users.size() == 0) {
                throw new NoSearchResultException("No users fulfilling criteria: " + "firstName = " + firstName
                        + ", lastName = " + lastName + ", username = " + username);
            } else {
                return users;
            }
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get users with criteria: firstName = " + firstName + ", lastName = "
                    + lastName + ", username = " + username, e);
        }
    }
    
    public Page<User> getAllByPage(int pageNumber, int pageSize) {
        Page<User> page;
        try {
            page = userRepository.findAll(new PageRequest(pageNumber, pageSize));
        } catch (Exception e) {
            throw new ServiceException("Failed to get users by page", e);
        }

        if (page != null) {
            return page;
        } else
            throw new NoSearchResultException("No users on page: " + pageNumber);
    }

    private boolean usernameLongEnough(String username) {
        final int minimumLengthUsername = 10;
        return username.length() >= minimumLengthUsername;
    }
}
