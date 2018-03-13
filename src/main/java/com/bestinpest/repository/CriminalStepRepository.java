package com.bestinpest.repository;

import com.bestinpest.model.CriminalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CriminalStepRepository  extends JpaRepository<CriminalStep, Long> {

    Optional<CriminalStep> findById(@Param("id") Long id);

}
