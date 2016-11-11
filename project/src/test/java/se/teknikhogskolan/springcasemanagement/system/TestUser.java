package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.teknikhogskolan.springcasemanagement.config.hsql.HsqlInfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.service.TeamService;
import se.teknikhogskolan.springcasemanagement.service.UserService;
import se.teknikhogskolan.springcasemanagement.service.WorkItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HsqlInfrastructureConfig.class })
@Transactional
public class TestUser {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private WorkItemService workItemService;

    private User luke;
    private User vader;
    private User leia;
    private User yoda;

    private Team darkSide;
    private Team lightSide;

    private WorkItem destroyDeathStar;
    private WorkItem trainToBeAYedi;

    @Before
    public void initializeDatabaseWithSomeValues() {

        darkSide = teamService.create("Dark side");
        lightSide = teamService.create("Light side");

        luke = userService.create(1L, "Robotarm Luke", "Luke", "Luke Skywalker");
        teamService.addUserToTeam(lightSide.getId(), luke.getId());

        vader = userService.create(2L, "I am your father", "Darth", "Vader");
        teamService.addUserToTeam(darkSide.getId(), vader.getId());

        leia = userService.create(3L, "I am your sister", "Leia", "Skywalker");
        teamService.addUserToTeam(lightSide.getId(), leia.getId());

        yoda = userService.create(4L, "Cool guy I am", "Yoda", "Unknown");
        teamService.addUserToTeam(lightSide.getId(), yoda.getId());

        destroyDeathStar = workItemService.create("Destroy death star");
        trainToBeAYedi = workItemService.create("Train to be a yedi");
        workItemService.setUser(luke.getUserNumber(), destroyDeathStar.getId());
        workItemService.setUser(luke.getUserNumber(), trainToBeAYedi.getId());
        workItemService.setStatus(destroyDeathStar.getId(), Status.STARTED);
        workItemService.setStatus(trainToBeAYedi.getId(), Status.DONE);
    }
    

    @Test
    public void canGetUserById() {
        assertEquals(luke, userService.getById(luke.getId()));
    }

    @Test
    public void canGetUserByNumber() {
        assertEquals(leia, userService.getByUserNumber(leia.getUserNumber()));
    }

    @Test
    public void canUpdateFirstName() {
        userService.updateFirstName(vader.getUserNumber(), "Anakin");
        User anakinVader = userService.getById(vader.getId());
        assertEquals("Anakin", anakinVader.getFirstName());
    }

    @Test
    public void canUpdateLastName() {
        userService.updateLastName(vader.getUserNumber(), "Skywalker");
        User darthSkywalker = userService.getById(vader.getId());
        assertEquals("Skywalker", darthSkywalker.getLastName());
    }

    @Test
    public void canUpdateUsername() {
        userService.updateUsername(vader.getUserNumber(), "I am your mother");
        User motherVader = userService.getById(vader.getId());
        assertEquals("I am your mother", motherVader.getUsername());
    }

    @Test
    public void inactivateUser() {
        User lukeBeforeInactivation = userService.getByUserNumber(luke.getUserNumber());
        assertEquals(true, lukeBeforeInactivation.isActive());
        User lukeAfterInactivation = userService.inactivate(luke.getUserNumber());
        assertEquals(false, lukeAfterInactivation.isActive());

        System.out.println(lukeAfterInactivation.getWorkItems());  //Skriver ut null trots eager fetch!
        
        for(WorkItem workItem : lukeAfterInactivation.getWorkItems()){
            assertEquals(Status.UNSTARTED, workItem.getStatus());
        }
    }
}
