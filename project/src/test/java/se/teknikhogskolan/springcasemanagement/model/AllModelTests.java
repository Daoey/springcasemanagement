package se.teknikhogskolan.springcasemanagement.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestIssueEntity.class, TestTeamEntity.class, TestUserEntity.class, TestWorkItem.class })
public class AllModelTests {}