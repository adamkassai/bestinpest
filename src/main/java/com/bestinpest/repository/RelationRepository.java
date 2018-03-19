package com.bestinpest.repository;

import com.bestinpest.model.Plan;
import com.bestinpest.model.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationRepository extends JpaRepository<Relation, Long> {

    Optional<Relation> findById(@Param("id") Long id);

}
