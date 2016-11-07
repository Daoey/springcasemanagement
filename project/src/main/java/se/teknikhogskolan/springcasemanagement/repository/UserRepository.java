package se.teknikhogskolan.springcasemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.teknikhogskolan.springcasemanagement.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUserNumber(Long userNumber);

    List<User> findByFirstNameContainingAndLastNameContainingAndUsernameContaining(String firstName, String lastName,
            String username);

    @Query("select u from User u where u.team.id = :teamId")
    List<User> findByTeamId(@Param("teamId") Long teamId);
}
