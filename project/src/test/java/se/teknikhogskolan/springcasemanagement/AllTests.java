package se.teknikhogskolan.springcasemanagement;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.teknikhogskolan.springcasemanagement.model.AllModelTests;
import se.teknikhogskolan.springcasemanagement.repository.AllRepositoryTests;
import se.teknikhogskolan.springcasemanagement.service.AllServiceTests;

@RunWith(Suite.class)
@SuiteClasses({AllServiceTests.class, AllRepositoryTests.class, AllModelTests.class})
public class AllTests {

}
