package se.teknikhogskolan.springcasemanagement.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public final class TestUser {
    
    @Test
    public void sameUserShouldBeEqual() {
        User user = new User(5L, "Username", "First name", "Last name");
        assertEquals(user, user);
    }

    @Test
    public void sameUserNumberAndUsernameShouldBeEqualAndProduceSameHashCode() {
        User user1 = new User(10L, "Same username", "first", "last");
        User user2 = new User(10L, "Same username", "other name", "other name");
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    public void sameUserNumberDifferentUsernameShouldNotBeEqual() {
        User user1 = new User(10L, "Same username", "first", "last");
        User user2 = new User(10L, "Other username", "other name", "other name");
        assertNotEquals(user1, user2);
    }
    
    @Test
    public void differentUsernumberSameUsernameShouldNotBeEqual() {
        User user1 = new User(10L, "Same username", "first", "last");
        User user2 = new User(5L, "Same username", "other name", "other name");
        assertNotEquals(user1, user2);
    }
    
    @Test
    public void differentObjectTypeShouldNotBeEqual() {
        User user1 = new User(10L, "Same username", "first", "last");
        String user2 = "Same username";
        assertNotEquals(user1, user2);
    }
}
