package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.service.exception.DatabaseException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotAllowedException;
import se.teknikhogskolan.springcasemanagement.service.exception.InvalidInputException;
import se.teknikhogskolan.springcasemanagement.service.exception.MaximumQuantityException;
import se.teknikhogskolan.springcasemanagement.service.exception.NotFoundException;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public Team getById(Long teamId) {
        return findTeam(teamId, String.format("Team with id '%d' do not exist", teamId));
    }

    public Team getByName(String teamName) {
        Team team;
        try {
            team = teamRepository.findByName(teamName);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Could not get team with name: %s", teamName), e);
        }

        if (team != null) {
            return team;
        } else {
            throw new NotFoundException(String.format("Team with name '%s' do not exist", teamName));
        }
    }

    public Team create(String teamName) {
        Team team = new Team(teamName);
        try {
            return teamRepository.save(team);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException(String.format("Team wit name '%s' already exist", teamName), e);
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Could not create team with name: %s", teamName));
        }
    }

    public Team updateName(Long teamId, String teamName) {
        Team team = findTeam(teamId, String.format("Team with id '%d' do not exist.", teamId));
        if (team.isActive()) {
            team.setName(teamName);
            return saveTeam(team, String.format("Could not update name on team with id: %d", teamId));
        } else {
            throw new NotAllowedException(String.format("Could not update "
                    + "name on team with id '%d' since it's inactive.", teamId));
        }
    }

    public Team activateTeam(Long teamId) {
        Team team = findTeam(teamId, String.format("Failed to activate team with id '%d'"
                + " since it could not be found in the database", teamId));
        team.setActive(true);
        return saveTeam(team, String.format("Could not activate team with id: %d", teamId));
    }

    public Team inactivateTeam(Long teamId) {
        Team team = findTeam(teamId, String.format("Failed to inactivate team with id '%d'"
                + " since it could not be found in the database", teamId));
        team.setActive(false);
        return saveTeam(team, String.format("Could not inactivate team with id: %d", teamId));
    }

    public Iterable<Team> getAll() {
        Iterable<Team> teams;
        try {
            teams = teamRepository.findAll();
        } catch (DataAccessException e) {
            throw new DatabaseException("Could not get all teams", e);
        }

        if (teams != null) {
            return teams;
        } else {
            throw new NotFoundException("No teams were found in the database");
        }
    }

    public Team addUserToTeam(Long teamId, Long userId) {
        User user = findUser(userId, String.format("User with id '%d' did not exist.", userId));
        Team team = findTeam(teamId, String.format("Team with id '%d' did not exist.", teamId));

        if (user.isActive() && team.isActive()) {
            if (team.getUsers().size() < 10) {
                user.setTeam(team);
                saveUser(user, String.format("Could not add user with id '%d' to team with id '%d'", userId, teamId));
                return findTeam(teamId, String.format("Team with id '%d' did not exist.", teamId));
            } else {
                throw new MaximumQuantityException(String.format("Team with id '%d' already contains 10 users",
                        teamId));
            }
        } else {
            throw new NotAllowedException(String.format("User with id '%d' or Team with id '%d' is inactive",
                    userId, teamId));
        }
    }

    public Team removeUserFromTeam(Long teamId, Long userId) {
        User user = findUser(userId, String.format("User with id '%d' did not exist.", userId));
        Team team = findTeam(teamId, String.format("Team with id '%d' did not exist.", teamId));

        if (user.isActive() && team.isActive()) {
            user.setTeam(null);
            saveUser(user, String.format("Could not remove user with id '%d' to team with id '%d'", userId, teamId));
            return findTeam(teamId, String.format("Team with id '%d' did not exist.", teamId));
        } else {
            throw new NotAllowedException(String.format("User with id '%d' or Team with id '%d' is inactive",
                    userId, teamId));
        }
    }

    private Team saveTeam(Team team, String databaseExceptionMessage) {
        try {
            return teamRepository.save(team);
        } catch (DataAccessException e) {
            throw new DatabaseException(databaseExceptionMessage, e);
        }
    }

    private User saveUser(User user, String databaseExceptionMessage) {
        try {
            return userRepository.save(user);
        } catch (DataAccessException e) {
            throw new DatabaseException(databaseExceptionMessage, e);
        }
    }

    private Team findTeam(Long teamId, String noSearchResultExceptionMessage) {
        try {
            Team team = teamRepository.findOne(teamId);
            if (team != null) {
                return team;
            } else {
                throw new NotFoundException(noSearchResultExceptionMessage);
            }
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Could not find team with id: %d", teamId), e);
        }
    }

    private User findUser(Long userId, String noSearchResultExceptionMessage) {
        try {
            User user = userRepository.findOne(userId);
            if (user != null) {
                return user;
            } else {
                throw new NotFoundException(noSearchResultExceptionMessage);
            }
        } catch (DataAccessException e) {
            throw new DatabaseException(String.format("Could not find user with id: %d", userId), e);
        }
    }
}