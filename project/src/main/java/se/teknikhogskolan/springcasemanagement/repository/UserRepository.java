package se.teknikhogskolan.springcasemanagement.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import se.teknikhogskolan.springcasemanagement.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUserNumber(Long userNumber);
    
    List<User> findByFirstNameContainingAndLastNameContainingAndUsernameContaining(String firstName, String lastName,
            String username);

}
