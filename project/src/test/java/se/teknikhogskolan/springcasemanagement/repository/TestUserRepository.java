package se.teknikhogskolan.springcasemanagement.repository;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;

import se.teknikhogskolan.springcasemanagement.model.Team;
import se.teknikhogskolan.springcasemanagement.model.User;

public final class TestUserRepository {

    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private User user = new User(1000L, "Long enough name", "First", "Last");

    @After
    public void cleanTest() {
        removeAllUsers();
    }

    @Test
    public void canSaveAndDeleteUsers() {

        Iterable<User> users = executeMultiple(userRepository -> userRepository.findAll());
        long numberOfUsersBeforeSave = users.spliterator().getExactSizeIfKnown();

        executeVoid(userRepository -> userRepository.save(user));

        users = executeMultiple(userRepository -> userRepository.findAll());
        long numberOfUsersAfterSave = users.spliterator().getExactSizeIfKnown();

        assertEquals(numberOfUsersBeforeSave, numberOfUsersAfterSave - 1);

        executeVoid(userRepository -> userRepository.delete(user));

        users = executeMultiple(userRepository -> userRepository.findAll());
        long numberOfUsersAfterDelete = users.spliterator().getExactSizeIfKnown();

        assertEquals(numberOfUsersBeforeSave, numberOfUsersAfterDelete);
    }

    @Test
    public void creatingTwoUsersWithSameUsernameCausesDataAccessException() {
        thrown.expect(DataAccessException.class);
        executeVoid(UserRepository -> UserRepository.save(user));
        executeVoid(
                UserRepository -> UserRepository.save(new User(1001L, user.getUsername(), "some name", "some name")));
    }

    @Test
    public void canGetUser() {
        executeVoid(userRepository -> userRepository.save(user));
        User userFromDatabase = execute(userRepository -> userRepository.findOne(user.getId()));
        assertEquals(user, userFromDatabase);
    }

    @Test
    public void canSearchForUsers() {

        long numberOfUsersToAdd = 5;
        for (long i = 0; i < numberOfUsersToAdd; i++) {
            Long index = i;
            executeVoid(userRepository -> userRepository.save(new User(user.getUserNumber() + index,
                    user.getUsername() + index, user.getFirstName(), user.getLastName())));
        }

        // Adding a similar user that should not be found
        executeVoid(userRepository -> userRepository.save(new User(100L, "username wrong", "firstNam", "llastname")));

        String firstName = user.getFirstName();
        String lastName = user.getLastName().substring(0, 2);
        String username = user.getUsername().substring(5, 9);

        Iterable<User> users = executeMultiple(userRepository -> userRepository
                .findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName, username));

        long numberOfUsersFound = users.spliterator().getExactSizeIfKnown();
        assertEquals(numberOfUsersToAdd, numberOfUsersFound);
    }

    @Test
    public void searchForUserNoUserFoundsReturnsEmptyList() {
        String firstName = user.getFirstName();
        String lastName = user.getLastName().substring(0, 2);
        String username = user.getUsername().substring(5, 9);
        Iterable<User> users = executeMultiple(userRepository -> userRepository
                .findByFirstNameContainingAndLastNameContainingAndUsernameContaining(firstName, lastName, username));
        users.spliterator().getExactSizeIfKnown();
    }

    @Test
    public void canGetAllByTeamId() {

        Team team;
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            team = teamRepository.save(new Team("New team"));
        }

        User tempUser1 = new User(2L, "long unique username1", "first", "last");
        User tempUser2 = new User(3L, "long unique username2", "first", "last");
        User tempUser3 = new User(4L, "long unique username3", "first", "last");

        tempUser1.setTeam(team);
        tempUser2.setTeam(team);
        tempUser3.setTeam(team);

        executeVoid(userRepository -> userRepository.save(tempUser1));
        executeVoid(userRepository -> userRepository.save(tempUser2));
        executeVoid(userRepository -> userRepository.save(tempUser3));

        Iterable<User> users = executeMultiple(userRepository -> userRepository.findByTeamId(team.getId()));
        long numberOfUsersFound = users.spliterator().getExactSizeIfKnown();
        assertEquals(3, numberOfUsersFound);

        // Need to remove all users pointing to team before removing the team
        removeAllUsers();

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            TeamRepository teamRepository = context.getBean(TeamRepository.class);
            teamRepository.delete(team.getId());
        }
    }

    @Test
    public void canFindUserByUserNumber() {
        executeVoid(userRepository -> userRepository.save(user));
        User userFromDatabase = execute(userRepository -> userRepository.findByUserNumber(user.getUserNumber()));
        assertEquals(user, userFromDatabase);
    }

    private void removeAllUsers() {
        Iterable<User> users = executeMultiple(userRepository -> userRepository.findAll());
        for (User user : users) {
            executeVoid(userRepository -> userRepository.delete(user));
        }
    }

    private Iterable<User> executeMultiple(Function<UserRepository, Iterable<User>> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            UserRepository userRepository = context.getBean(UserRepository.class);
            return operation.apply(userRepository);
        }
    }

    private User execute(Function<UserRepository, User> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            UserRepository userRepository = context.getBean(UserRepository.class);
            return operation.apply(userRepository);
        }
    }

    private void executeVoid(Consumer<UserRepository> operation) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan(PROJECT_PACKAGE);
            context.refresh();
            UserRepository userRepository = context.getBean(UserRepository.class);
            operation.accept(userRepository);
        }
    }
}
