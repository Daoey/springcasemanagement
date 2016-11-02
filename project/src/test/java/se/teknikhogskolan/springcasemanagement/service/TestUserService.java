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

            Team team = new Team("teamname");
            
            User user1 = new User(1L, "username1", "firstname", "lastname", team);
            User user2 = new User(2L, "username2", "firstname", "lastname", team);
            User user3 = new User(3L, "username3", "firstname", "lastname", team);

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

            User user = new User(4L, "username", "firstname", "lastname", new Team("teamname"));
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
