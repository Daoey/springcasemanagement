package se.teknikhogskolan.springcasemanagement.repository;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestIssueRepository.class, TestTeamRepository.class, TestUserRepository.class,
        TestWorkItemRepository.class})
public final class AllRepositoryTests {
}