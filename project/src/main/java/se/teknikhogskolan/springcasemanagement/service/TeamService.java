package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;

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
        Team team;
        try {
            team = teamRepository.findOne(teamId);
        } catch (Exception e) {
            throw new ServiceException("Could not get team with id: " + teamId, e);
        }

        if (team != null) {
            return team;
        } else
            throw new NoSearchResultException("Team with id '" + teamId + "' do not exist");
    }

    public Team getByName(String teamName) {
        Team team;
        try {
            team = teamRepository.findByName(teamName);
        } catch (Exception e) {
            throw new ServiceException("Could not get team with name: " + teamName, e);
        }

        if (team != null) {
            return team;
        } else
            throw new NoSearchResultException("Team with name '" + teamName + "' do not exist");
    }

    public Team create(String teamName) {
        Team team = new Team(teamName);
        try {
            return teamRepository.save(team);
        } catch (DuplicateKeyException e) {
            throw new ServiceException("Team wit name '" + teamName + "' already exist", e);
        } catch (Exception e) {
            throw new ServiceException("Could not create team with name: " + teamName, e);
        }
    }

    public Team updateName(Long teamId, String teamName) {
        try {
            Team team = teamRepository.findOne(teamId);
            if (team.isActive()) {
                team.setName(teamName);
                return teamRepository.save(team);
            } else
                throw new ServiceException("Could not update "
                        + "name on team with id '" + teamId + "' since it's inactive.");
        } catch (ServiceException e) {
            throw e;
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Team with id '" + teamId + "' do not exist.");
        } catch (Exception e) {
            throw new ServiceException("Could not update name on team with id: " + teamId, e);
        }
    }

    public Team inactive(Long teamId) {
        try {
            Team team = teamRepository.findOne(teamId);
            team.setActive(false);
            return teamRepository.save(team);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Failed to inactive team with id '"
                    + teamId + "' since it could not be found in the database", e);
        } catch (Exception e) {
            throw new ServiceException("Could not inactive team with id: " + teamId, e);
        }
    }

    public Team activate(Long teamId) {
        try {
            Team team = teamRepository.findOne(teamId);
            team.setActive(true);
            return teamRepository.save(team);
        } catch (NullPointerException e) {
            throw new NoSearchResultException("Failed to activate team with id '"
                    + teamId + "' since it could not be found in the database", e);
        } catch (Exception e) {
            throw new ServiceException("Could not activate team with id: " + teamId, e);
        }
    }

    public Iterable<Team> getAll() {
        Iterable<Team> teams;
        try {
            teams = teamRepository.findAll();
            if (teams == null)
                 throw new NoSearchResultException("No teams were found in the database");
            return teams;
        } catch (NoSearchResultException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Could not get all teams", e);
        }
    }

    public Team addUserToTeam(Long teamId, Long userId) {
        try {
            User user = userRepository.findOne(userId);
            Team team = teamRepository.findOne(teamId);

            if (team == null || user == null) {
                throw new NoSearchResultException("Team with id '"
                        + teamId + "' or User with id '" + userId + "' did not exist.");
            } else if (!user.isActive() || !team.isActive()) {
                throw new ServiceException("User with id '"
                        + userId + "' or Team with id '" + teamId + "' is inactive");
            } else {
                if (team.getUsers().size() < 10) {
                    user.setTeam(team);
                    userRepository.save(user);
                    return teamRepository.findOne(teamId);
                } else {
                    throw new ServiceException("Team with id '" + teamId + "' already contains 10 users");
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Could not add user with id '" + userId
                    + "' to team with id '" + teamId, e);
        }
    }

    public Team removeUserFromTeam(Long teamId, Long userId) {
        try {
            User user = userRepository.findOne(userId);
            Team team = teamRepository.findOne(teamId);

            if (team == null || user == null) {
                throw new NoSearchResultException("Team with id '"
                        + teamId + "' or User with id '" + userId + "' did not exist.");
            } else if (!user.isActive() || !team.isActive()) {
                throw new ServiceException("User with id '"
                        + userId + "' or Team with id '" + teamId + "' is inactive");
            } else {
                user.setTeam(null);
                userRepository.save(user);
                return teamRepository.findOne(teamId);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Could not remove user with id '" + userId
                    + "' from team with id '" + teamId, e);
        }
    }
}