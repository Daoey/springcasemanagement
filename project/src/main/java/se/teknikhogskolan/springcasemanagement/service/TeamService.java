package se.teknikhogskolan.springcasemanagement.service;

import org.hibernate.service.spi.ServiceException;
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

    public Team createTeam(String name){
        return teamRepository.save(new Team(name));
    }

    public Team updateTeam(Long id, String name){
        Team team = teamRepository.findOne(id);
        team.setName(name);
        return teamRepository.save(team);
    }

    public Team inactiveTeam(Long id) {
        Team team = teamRepository.findOne(id);
        team.setActive(false);
        return teamRepository.save(team);
    }

    public Team activateTeam(Long id) {
        Team team = teamRepository.findOne(id);
        team.setActive(true);
        return teamRepository.save(team);
    }

    public Iterable<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    public Team addUserToTeam(Long teamId, Long userId){
        User user = userRepository.findOne(userId);
        Team team = teamRepository.findOne(teamId);

        if(user.isActive() && team.isActive()){
            if(team.getUsers().size() < 10){
                user.setTeam(team);
                userRepository.save(user);
                return teamRepository.findOne(teamId);
            }else{
                throw new ServiceException("Team with id '" + teamId + "' already contains 10 users");
            }
        }else{
            throw new ServiceException("User with id '" + userId + "' or Team with id '" + teamId + "' is inactive");
        }
    }
}
