package se.teknikhogskolan.springcasemanagement.repository;

import org.springframework.data.repository.CrudRepository;

import se.teknikhogskolan.springcasemanagement.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
