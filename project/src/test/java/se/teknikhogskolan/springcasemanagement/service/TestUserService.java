package se.teknikhogskolan.springcasemanagement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;

public final class TestUserService {

    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement";

    @BeforeClass
    public static void init() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            UserService userService = context.getBean(UserService.class);
            TeamService teamService = context.getBean(TeamService.class);

            Team team;
            if (teamService.getAllTeams().iterator().hasNext()) {
                team = teamService.getAllTeams().iterator().next();
            } else {
                team = new Team("Test team");
            }

            User user1 = new User(1L, "longusername1", "firstname", "lastname", team);
            User user2 = new User(2L, "longusername2", "firstname", "lastname", team);
            User user3 = new User(3L, "longusername3", "firstname", "lastname", team);

            userService.saveUser(user1);
            userService.saveUser(user2);
            userService.saveUser(user3);
        }
    }

    @AfterClass
    public static void clean() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            UserService userService = context.getBean(UserService.class);

            userService.deleteUser(1L);
            userService.deleteUser(2L);
            userService.deleteUser(3L);
        }
    }

    @Test
    public void canSaveUser() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            UserService userService = context.getBean(UserService.class);
            TeamService teamService = context.getBean(TeamService.class);
            Team team = teamService.getAllTeams().iterator().next();

            User user = new User(4L, "longusername4", "firstname", "lastname", team);

            user = userService.saveUser(user);

            assertNotNull(user.getId());

            userService.deleteUser(4L);
        }
    }

    @Test
    public void canSearchForUser() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();

            UserService userService = context.getBean(UserService.class);

            List<User> users = userService.searchUsers("first", "last", "user");

            assertEquals(3, users.size());
        }
    }

}
