package com.bestinpest.repository;

import com.bestinpest.model.Route;
import com.bestinpest.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findById(@Param("id") Long id);

    List<Route> findByArrival(@Param("arrival") Stop arrival);

    List<Route> findByDeparture(@Param("departure") Stop departure);

}
