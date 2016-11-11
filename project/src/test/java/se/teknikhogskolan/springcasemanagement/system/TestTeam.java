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
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HsqlInfrastructureConfig.class })
@Sql("data.sql")
@Transactional
public class TestTeam {

    private String name;
    private Long id;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        this.name = "test";
        this.id = 1L;
    }

    @Test
    public void canGetTeamById() {
        Team teamFromDb = teamService.getById(id);
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
        Team updatedTeam = teamService.updateName(id, newName);
        assertEquals(updatedTeam.getName(), newName);
    }

    @Test
    public void canInactiveTeam() throws Exception {
        Team teamFromDb = teamService.inactive(id);
        assertFalse(teamFromDb.isActive());
    }

    @Test
    public void canActivateTeam() throws Exception {
        Team inactiveTeamFromDb = teamService.inactive(id);
        assertFalse(inactiveTeamFromDb.isActive());
        Team activeTeamFromDb = teamService.activate(id);
        assertTrue(activeTeamFromDb.isActive());
    }

    @Test
    public void canGetAllTeams() throws Exception {
        Iterable<Team> teams = teamService.getAll();
        assertNotNull(teams);
    }

    @Test
    public void canAddUserToTeam() throws Exception {
        Team team = teamService.addUserToTeam(id, 1L);
        User user = userService.getByUserNumber(1L);
        System.out.println(user.getTeam());
    }

    @Test
    public void canRemoveUserFromTeam() throws Exception {

    }
}