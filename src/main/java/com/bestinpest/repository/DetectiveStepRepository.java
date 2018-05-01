package com.bestinpest.repository;

import com.bestinpest.model.DetectiveStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetectiveStepRepository extends JpaRepository<DetectiveStep, Long> {

    Optional<DetectiveStep> findById(@Param("id") Long id);

}
