package se.teknikhogskolan.springcasemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
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
        Team team = teamRepository.findOne(teamId);
        if (team != null) {
            return team;
        } else
            throw new ServiceException("Team with id '" + teamId + "' do not exist");
    }

    public Team getByName(String teamName) {
        Team team = teamRepository.findByName(teamName);
        if (team != null) {
            return team;
        } else
            throw new ServiceException("Team with name '" + teamName + "' do not exist");
    }

    public Team create(String teamName) {
        Team team = new Team(teamName);
        return teamRepository.save(team);
    }

    public Team updateName(Long teamId, String teamName) {
        Team team = teamRepository.findOne(teamId);
        if (team != null) {
            if (team.isActive()) {
                team.setName(teamName);
                return teamRepository.save(team);
            } else
                throw new ServiceException("Could not update "
                        + "name on team with id '" + teamId + "' since it's inactive.");
        } else
            throw new ServiceException("Team with id '" + teamId + "' did not exist.");
    }

    public Team inactive(Long teamId) {
        Team team = teamRepository.findOne(teamId);
        if (team != null) {
            team.setActive(false);
            return teamRepository.save(team);
        } else
            throw new ServiceException("Team with id '" + teamId + "' did not exist.");
    }

    public Team activate(Long teamId) {
        Team team = teamRepository.findOne(teamId);
        if (team != null) {
            team.setActive(true);
            return teamRepository.save(team);
        } else
            throw new ServiceException("Team with id '" + teamId + "' did not exist.");
    }

    public Iterable<Team> getAll() {
        return teamRepository.findAll();
    }

    public Team addUserToTeam(Long teamId, Long userId) {
        User user = userRepository.findOne(userId);
        Team team = teamRepository.findOne(teamId);

        if (team == null || user == null) {
            throw new ServiceException("Team with id '"
                    + teamId + "' or User with id '" + userId + "' did not exist.");
        } else if (!user.isActive() || !team.isActive()) {
            throw new ServiceException("User with id '"
                    + userId + "' or Team with id '" + teamId + "' is inactive");
        } else {
            if (team.getUsers().size() < 10) {
                System.out.println(team.getUsers());
                user.setTeam(team);
                userRepository.save(user);
                return teamRepository.findOne(teamId);
            } else {
                throw new ServiceException("Team with id '" + teamId + "' already contains 10 users");
            }
        }
    }

    public Team removeUserFromTeam(Long teamId, Long userId) {
        User user = userRepository.findOne(userId);
        Team team = teamRepository.findOne(teamId);

        if (team == null || user == null) {
            throw new ServiceException("Team with id '"
                    + teamId + "' or User with teamId '" + userId + "' did not exist.");
        } else if (!user.isActive() || !team.isActive()) {
            throw new ServiceException("User with id '"
                    + userId + "' or Team with teamId '" + teamId + "' is inactive");
        } else {
            user.setTeam(null);
            userRepository.save(user);
            return teamRepository.findOne(teamId);
        }
    }
}