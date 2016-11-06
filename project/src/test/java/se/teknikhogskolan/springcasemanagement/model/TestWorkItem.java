package se.teknikhogskolan.springcasemanagement.model;


import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import se.teknikhogskolan.springcasemanagement.model.WorkItem.Status;

public class TestWorkItem {
    private static WorkItem workItem1;
    private static WorkItem workItem1WithValues;
    private static WorkItem workItem2;
    
    @BeforeClass
    public static void masterSetup() {
        workItem1 = new WorkItem("First WorkItem");
        workItem1WithValues = new WorkItem("First WorkItem");
        workItem1WithValues.setStatus(Status.STARTED);
        workItem1WithValues.setIssue(new Issue("This is an issue"));
        workItem1WithValues.setUser(new User(1L, "username", "firstName", "lastName", null));
        workItem2 = new WorkItem("Second WorkItem");
    }

    @Test
    public void testingEquals() {
        assertEquals(workItem1, workItem1);
        assertEquals(workItem1, workItem1WithValues);
        assertNotEquals(workItem1, workItem2);
    }

    private void assertNotEquals(Object o1, Object o2) {
        assertEquals(false, o1.equals(o2));
    }
    
    @Test
    public void testingHashCode() {
        assertEquals(workItem1.hashCode(), workItem1.hashCode());
        assertEquals(workItem1.hashCode(), workItem1WithValues.hashCode());
        assertNotEquals(workItem1.hashCode(), workItem2.hashCode());
    }
    
    @Test
    public void testingCompareTo() {
        assertEquals(0, workItem1.compareTo(workItem1));
        assertEquals(0, workItem1.compareTo(workItem1WithValues));
        assertEquals(-1, workItem1.compareTo(workItem2));
        assertEquals(1, workItem2.compareTo(workItem1));
    }
}
