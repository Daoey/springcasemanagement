package se.teknikhogskolan.springcasemanagement.repository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import se.teknikhogskolan.springcasemanagement.model.Issue;
import se.teknikhogskolan.springcasemanagement.model.Team;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class TestTeamRepository {

    private final String projectPackage = "se.teknikhogskolan.springcasemanagement";
    private Team team;

    @Before
    public void setUp() throws Exception {
        this.team = new Team("Test");
    }

    @Test
    public void canSaveTeam() throws Exception {
        executeVoid(teamRepository -> teamRepository.save(team));
        deleteTeam(team);
    }

    @Test
    public void canGetTeam() throws Exception {
        Team teamFromDb = execute(teamRepository -> {
            team.setName("new test team");
            teamRepository.save(team);
            return teamRepository.findOne(team.getId());
        });

        assertEquals(team, teamFromDb);
        deleteTeam(team);
    }

    private Team execute(Function<TeamRepository, Team> operation){
        try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()){
            context.scan(projectPackage);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            return operation.apply(teamRepository);
        }
    }

    private void executeVoid(Consumer<TeamRepository> operation){
        try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()){
            context.scan(projectPackage);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            operation.accept(teamRepository);
        }
    }

    private void deleteTeam(Team team){
        executeVoid(teamRepository -> teamRepository.delete(team));
    }
}
