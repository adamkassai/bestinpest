package com.bestinpest.repository;

import com.bestinpest.model.Route;
import com.bestinpest.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByArrival(@Param("arrival") Stop arrival);

    List<Route> findByDeparture(@Param("departure") Stop departure);

}
