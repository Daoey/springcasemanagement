package se.teknikhogskolan.springcasemanagement.repository;

import org.springframework.data.repository.CrudRepository;

import se.teknikhogskolan.springcasemanagement.model.Team;

public interface TeamRepository extends CrudRepository<Team, Long> {

    Team findByName(String name);
}