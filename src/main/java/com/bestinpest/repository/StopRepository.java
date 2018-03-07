package com.bestinpest.repository;

import com.bestinpest.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop, Long> {

    Optional<Stop> findById(@Param("id") String id);

}
