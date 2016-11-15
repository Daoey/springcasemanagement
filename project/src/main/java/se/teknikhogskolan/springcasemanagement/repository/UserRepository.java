package se.teknikhogskolan.springcasemanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.teknikhogskolan.springcasemanagement.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByUserNumber(Long userNumber);

    // TODO shorten name
    List<User> findByFirstNameContainingAndLastNameContainingAndUsernameContaining(String firstName, String lastName,
                                                                                   String username);

    Page<User> findAll(Pageable pageable);

    @Query("select u from User u where u.team.id = :teamId")
    List<User> findByTeamId(@Param("teamId") Long teamId);

    @Query("select u from User u where u.created between :startDate and :endDate")
    List<User> findByCreationDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}