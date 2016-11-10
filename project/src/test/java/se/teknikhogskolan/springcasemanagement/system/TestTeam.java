package se.teknikhogskolan.springcasemanagement.system;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.teknikhogskolan.springcasemanagement.config.hsql.HsqlInfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.service.TeamService;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTeam {

    private Team createdTeam;

    @Before
    public void setUp() throws Exception {
        createdTeam = execute(teamService -> teamService.create("name"));
    }

    @Test
    public void canGetTeamById() {
        Team teamFromDb = execute(teamService -> teamService.getById(createdTeam.getId()));
        assertEquals(teamFromDb, createdTeam);
    }

    @Test
    public void canGetTeamByName() throws Exception {
        Team teamFromDb = execute(teamService -> teamService.getByName(createdTeam.getName()));
        assertEquals(teamFromDb, createdTeam);
    }

    @Test
    public void canUpdateName() throws Exception {
        String newName = "Updated name";
        Team teamFromDb = execute(teamService -> teamService.updateName(createdTeam.getId(), newName));
        assertEquals(teamFromDb.getName(), newName);
    }

    @Test
    public void canInactiveTeam() throws Exception {
        Team teamFromDb = execute(teamService -> teamService.inactive(createdTeam.getId()));
        assertFalse(teamFromDb.isActive());
    }

    @Test
    public void canActiveTeam() throws Exception {
        Team teamFromDb = execute(teamService -> teamService.activate(createdTeam.getId()));
        assertTrue(teamFromDb.isActive());
    }

    @After
    public void tearDown() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(HsqlInfrastructureConfig.HSQL_PROJECT_PACKAGE);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            teamRepository.delete(createdTeam);
        }
    }

    private Team execute(Function<TeamService, Team> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(HsqlInfrastructureConfig.HSQL_PROJECT_PACKAGE);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            UserRepository userRepository = context.getBean(UserRepository.class);
            return operation.apply(new TeamService(teamRepository, userRepository));
        }
    }
}
