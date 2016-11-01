package se.teknikhogskolan.springcasemanagement;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.teknikhogskolan.springcasemanagement.model.TestUser;
import se.teknikhogskolan.springcasemanagement.repository.TestUserRepository;
import se.teknikhogskolan.springcasemanagement.service.TestUserService;

@RunWith(Suite.class)
@SuiteClasses({TestUserRepository.class, TestUserService.class, TestUser.class})
public class AllTests {

}
