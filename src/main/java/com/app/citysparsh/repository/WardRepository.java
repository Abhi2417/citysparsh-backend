package com.app.citysparsh.repository;

import com.app.citysparsh.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findByZone(String zone);

    Optional<Ward> findByWardNumber(Integer wardNumber);

    @Query("SELECT w FROM Ward w WHERE " +
            ":lat BETWEEN w.minLat AND w.maxLat AND " +
            ":lng BETWEEN w.minLng AND w.maxLng")
    List<Ward> findByCoordinates(
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );
}
