package se.teknikhogskolan.springcasemanagement.system;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.repository.TeamRepository;
import se.teknikhogskolan.springcasemanagement.repository.UserRepository;
import se.teknikhogskolan.springcasemanagement.service.TeamService;

import static org.junit.Assert.assertEquals;


public class TestTeam {

    private TeamService teamService;
    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement.config.hsql";

    @Before
    public void setUp() {
        teamService = new TeamService(getTeamRepository(), getUserRepository());
    }

    @Test
    public void canCreateTeam() {
        Team team = teamService.create("team");
        assertEquals(team.getName(), "team");
    }

    private TeamRepository getTeamRepository(){
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            return context.getBean(TeamRepository.class);
        }
    }

    private UserRepository getUserRepository(){
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            return context.getBean(UserRepository.class);
        }
    }
}
