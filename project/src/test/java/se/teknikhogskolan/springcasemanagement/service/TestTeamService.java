package se.teknikhogskolan.springcasemanagement.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TestTeamService {

    @Mock
    TeamRepository teamRepository;

    @InjectMocks
    TeamService teamService;

    private Team teamInDb;
    private Team team;
    private Long teamId;

    @Before
    public void setUp() throws Exception {
        this.teamInDb = new Team("Team in db");
        this.team = new Team("Team");
        this.teamId = 5L;
    }

    @Test
    public void canGetTeamByName() throws Exception {

    }

    @Test
    public void canGetTeamById() throws Exception {

    }

    @Test
    public void canSaveTeam() throws Exception {
        teamService.saveTeam(team);
        verify(teamRepository).save(team);
    }

    @Test
    public void canUpdateTeamName() throws Exception {
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        when(teamRepository.save(teamInDb)).thenReturn(teamInDb);
        teamInDb.setName("Old name");
        Team team = teamService.updateTeamName(teamId, newName);

        verify(teamRepository).save(teamInDb);
        assertEquals(team.getName(), newName);
    }

    @Test(expected = ServiceException.class)
    public void canNotUpdateTeamNameIfTeamIsInactive() throws Exception {
        String newName = "New name";
        when(teamRepository.findOne(teamId)).thenReturn(teamInDb);
        teamInDb.setActive(false);
        teamService.updateTeamName(teamId, newName);
    }

    @Test
    public void canInactivateTeam() throws Exception {

    }

    @Test
    public void canActivateTeam() throws Exception {

    }

    @Test
    public void canGetAllTeams() throws Exception {

    }

    @Test
    public void throwExceptionIfTeamIdOrUserIdIsNull() throws Exception {

    }

    @Test
    public void throwExceptionIfTeamOrUserIsInactive() throws Exception {

    }

    @Test
    public void canNotAddUserToTeamIfTeamIsFull() throws Exception {

    }

    @Test
    public void canAddUserToTeam() throws Exception {

    }
}
