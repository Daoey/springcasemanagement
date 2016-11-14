package se.teknikhogskolan.springcasemanagement.system;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestIssueIntegration.class, TestTeamIntegration.class, TestUserIntegration.class, TestWorkItemIntegration.class })
public class AllIntegrationTests {
}
