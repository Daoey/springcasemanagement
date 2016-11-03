package se.teknikhogskolan.springcasemanagement.repository;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import se.teknikhogskolan.springcasemanagement.model.User;

public final class TestUserRepository {

    private static final String PROJECT_PACKAGE = "se.teknikhogskolan.springcasemanagement";

    private User user = new User(1000L, "Long enough name", "First", "Last", null);

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
