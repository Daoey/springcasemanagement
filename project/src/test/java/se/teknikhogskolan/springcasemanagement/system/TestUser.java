package se.teknikhogskolan.springcasemanagement.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.teknikhogskolan.springcasemanagement.config.hsql.HsqlInfrastructureConfig;
import se.teknikhogskolan.springcasemanagement.model.User;
import se.teknikhogskolan.springcasemanagement.model.WorkItem;
import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;
import se.teknikhogskolan.springcasemanagement.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HsqlInfrastructureConfig.class })
@SqlGroup({
    @Sql(scripts = "insert_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "clean_user_hsql.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class TestUser {

    @Autowired
    private UserService userService;

    //Same as the the initialisation in insert_user.sql
    private final User luke = new User(1L, "Robotarm Luke", "Luke", "Skywalker");
    private final User vader = new User(2L, "I am your father", "Darth", "Vader");
    private final User leia = new User(3L, "I am your sister", "Leia", "Skywalker");
    private final User yoda = new User(4L, "Master Yoda", "Yoda", "");

    @Test
    public void canCreateNewUser() {
        User han = new User(5L, "Captain Solo", "Han", "Solo");
        User databaseHan = userService.create(han.getUserNumber(), han.getUsername(), han.getFirstName(), han.getLastName());
        assertEquals(han, databaseHan);
    }
    
    @Test
    public void canGetUserById() {
        assertEquals(luke, userService.getById(10L));
    }

    @Test
    public void canGetUserByNumber() {
        assertEquals(leia, userService.getByUserNumber(leia.getUserNumber()));
    }

    @Test
    public void canUpdateFirstName() {
        userService.updateFirstName(vader.getUserNumber(), "Anakin");
        User anakinVader = userService.getByUserNumber(vader.getUserNumber());
        assertEquals("Anakin", anakinVader.getFirstName());
    }

    @Test
    public void canUpdateLastName() {
        userService.updateLastName(vader.getUserNumber(), "Skywalker");
        User darthSkywalker = userService.getByUserNumber(vader.getUserNumber());
        assertEquals("Skywalker", darthSkywalker.getLastName());
    }

    @Test
    public void canUpdateUsername() {
        userService.updateUsername(vader.getUserNumber(), "I am your mother");
        User motherVader = userService.getByUserNumber(vader.getUserNumber());
        assertEquals("I am your mother", motherVader.getUsername());
    }

    @Test
    public void inactivateAndActivateUser() {
        
        User lukeBeforeInactivation = userService.getByUserNumber(luke.getUserNumber());
        assertEquals(true, lukeBeforeInactivation.isActive());
        
        User lukeAfterInactivation = userService.inactivate(luke.getUserNumber());
        assertEquals(false, lukeAfterInactivation.isActive());

        for(WorkItem workItem : lukeAfterInactivation.getWorkItems()){
            assertEquals(Status.UNSTARTED, workItem.getStatus());
        }
        
        User lukeAfterReActivation = userService.activate(luke.getUserNumber());
        assertEquals(true, lukeAfterReActivation.isActive());
    }
    
    @Test
    public void getAllByTeamId() {
        List<User> lightSideUsers = new ArrayList<User>();
        lightSideUsers.add(luke);
        lightSideUsers.add(leia);
        lightSideUsers.add(yoda);
        //Team id 1L is the light side team
        List<User> lightSideUsersFromDataBase = userService.getAllByTeamId(1L);
        assertEquals(lightSideUsers.size(), lightSideUsersFromDataBase.size());
        assertTrue(lightSideUsersFromDataBase.contains(luke));
        assertTrue(lightSideUsersFromDataBase.contains(leia));
        assertTrue(lightSideUsersFromDataBase.contains(yoda));
    }
    
    @Test
    public void search() {
        List<User> lastNameSkywalker = new ArrayList<User>();
        lastNameSkywalker.add(luke);
        lastNameSkywalker.add(leia);
        List<User> usersFromDataBase = userService.search("", "Skywalker", "");
        
        assertEquals(lastNameSkywalker.size(), usersFromDataBase.size());
        assertTrue(usersFromDataBase.contains(luke));
        assertTrue(usersFromDataBase.contains(leia));        
    }
    
    @Test
    public void getAllByPage(){
        Page<User> userPage = userService.getAllByPage(0, 2);
        assertEquals(2, userPage.getNumberOfElements());
        assertEquals(2, userPage.getTotalPages());
        
        List<User> allUsers = new ArrayList<>();
        allUsers.add(luke);
        allUsers.add(leia);
        allUsers.add(vader);
        allUsers.add(yoda);
        
        assertEquals(allUsers.size(), userPage.getTotalElements());
    }
    
    @Test
    public void getByCreationDate(){
        List<User> usersFromDatabase = userService.getByCreationDate(LocalDate.of(2016, 11, 11), LocalDate.of(2016, 11, 12));
        assertEquals(2, usersFromDatabase.size());
        assertTrue(usersFromDatabase.contains(luke));
        assertTrue(usersFromDatabase.contains(vader));
    }
    
}
