package se.teknikhogskolan.springcasemanagement.system;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.teknikhogskolan.springcasemanagement.config.hsql.HsqlInfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.service.TeamService;
import se.teknikhogskolan.springcasemanagement.service.UserService;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HsqlInfrastructureConfig.class })
@Sql("team.sql")
@Transactional
public class TestTeam {

    private String name;
    private Long teamId;
    private Long userId;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        this.name = "test";
        this.teamId = 1L;
        this.userId = 1L;
    }

    @Test
    public void canGetTeamById() {
        Team teamFromDb = teamService.getById(teamId);
        assertNotNull(teamFromDb);
    }

    @Test
    public void canGetTeamByName() throws Exception {
        Team teamFromDb = teamService.getByName(name);
        assertNotNull(teamFromDb);
    }

    @Test
    public void canUpdateName() throws Exception {
        String newName = "Updated name";
        Team updatedTeam = teamService.updateName(teamId, newName);
        assertEquals(updatedTeam.getName(), newName);
    }

    @Test
    public void canInactiveTeam() throws Exception {
        Team teamFromDb = teamService.inactive(teamId);
        assertFalse(teamFromDb.isActive());
    }

    @Test
    public void canActivateTeam() throws Exception {
        canInactiveTeam();
        Team activeTeamFromDb = teamService.activate(teamId);
        assertTrue(activeTeamFromDb.isActive());
    }

    @Test
    public void canGetAllTeams() throws Exception {
        Iterable<Team> teams = teamService.getAll();
        assertNotNull(teams);
    }

    @Test
    public void canAddUserToTeam() throws Exception {
        Team team = teamService.addUserToTeam(teamId, userId);
        User user = userService.getByUserNumber(userId);
        assertEquals(user.getTeam(), team);
    }

    @Test
    public void canRemoveUserFromTeam() throws Exception {
        canAddUserToTeam();
        teamService.removeUserFromTeam(teamId, userId);
        User user = userService.getByUserNumber(userId);
        assertNull(user.getTeam());
    }
}