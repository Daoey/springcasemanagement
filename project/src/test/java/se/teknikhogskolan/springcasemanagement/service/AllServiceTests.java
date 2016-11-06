package se.teknikhogskolan.springcasemanagement.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestIssueService.class, TestTeamService.class, TestUserService.class, TestWorkItemService.class })
public class AllServiceTests {}