package se.teknikhogskolan.springcasemanagement.system;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.hsql.HsqlInfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.service.TeamService;
import se.teknikhogskolan.springcasemanagement.service.UserService;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HsqlInfrastructureConfig.class })
@SqlGroup({ @Sql(scripts = "insert_team.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "hsql_clean_tables.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
public class TestTeamIntegration {

    // Same as in the insert_team.sql file
    private String name;
    private Long teamId;
    private Long userId;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        this.name = "test";
        this.teamId = 1L;
        this.userId = 1L;
    }
    
    @Test
    public void teamToString() {
        final String result = teamService.getById(teamId).toString();
        final String expectedToString = "Team [id=1, name=test, active=true, "
                + "usersSize=0, created=2016-11-11, lastModified=null]";
        assertEquals(result, expectedToString);
    }

    @Test
    public void canGetTeamById() {
        Team teamFromDb = teamService.getById(teamId);
        assertNotNull(teamFromDb);
    }

    @Test
    public void canGetTeamByName() {
        Team teamFromDb = teamService.getByName(name);
        assertNotNull(teamFromDb);
    }

    @Test
    public void canUpdateName() {
        String newName = "Updated name";
        Team updatedTeam = teamService.updateName(teamId, newName);
        assertEquals(updatedTeam.getName(), newName);
    }

    @Test
    public void canInactiveTeam() {
        Team teamFromDb = teamService.setTeamActive(false, teamId);
        assertFalse(teamFromDb.isActive());
    }

    @Test
    public void canActivateTeam() {
        Team inactiveTeamFromDb = teamService.setTeamActive(false, teamId);
        assertFalse(inactiveTeamFromDb.isActive());
        Team activeTeamFromDb = teamService.setTeamActive(true, teamId);
        assertTrue(activeTeamFromDb.isActive());
    }

    @Test
    public void canGetAllTeams() {
        Iterable<Team> teams = teamService.getAll();
        assertNotNull(teams);
    }

    @Test
    public void canAddUserToTeam() {
        Team team = teamService.addUserToTeam(teamId, userId);
        User user = userService.getByUserNumber(userId);
        assertEquals(user.getTeam(), team);
    }

    @Test
    public void canRemoveUserFromTeam() {
        canAddUserToTeam();
        teamService.removeUserFromTeam(teamId, userId);
        User user = userService.getByUserNumber(userId);
        assertNull(user.getTeam());
    }
}