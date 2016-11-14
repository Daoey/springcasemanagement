package se.teknikhogskolan.springcasemanagement.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

public final class TestIssueEntity {

    private Issue issue;

    @Before
    public void setUp(){
        issue = new Issue("description");
    }

    @Test
    public void sameTeamShouldBeEqual() {
        assertEquals(issue, issue);
    }

    @Test
    public void differentDescriptionsShouldNotBeEqual() {
        Issue newIssue = new Issue("diff description");
        assertNotEquals(newIssue, issue);
    }

    @Test
    public void sameDescriptionShouldHaveSameHashCode() {
        Issue newIssue = new Issue(issue.getDescription());
        assertEquals(newIssue.hashCode(), issue.hashCode());
    }

    @Test
    public void differentObjectTypeShouldNotBeEqual() {
        String issueString = issue.getDescription();
        assertNotEquals(issueString, issue);
    }
}