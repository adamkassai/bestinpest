package com.bestinpest.repository;

import com.bestinpest.model.Junction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JunctionRepository extends JpaRepository<Junction, String> {

    Optional<Junction> findById(@Param("id") String id);

}
