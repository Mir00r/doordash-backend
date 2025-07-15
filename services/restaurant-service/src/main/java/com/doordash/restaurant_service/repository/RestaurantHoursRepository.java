package com.doordash.restaurant_service.repository;

import com.doordash.restaurant_service.model.RestaurantHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantHoursRepository extends JpaRepository<RestaurantHours, Long> {

    // Find restaurant hours by restaurant id
    List<RestaurantHours> findByRestaurantId(Long restaurantId);

    // Find restaurant hours by restaurant id and day of week
    Optional<RestaurantHours> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    // Find restaurant hours by restaurant id and is closed
    List<RestaurantHours> findByRestaurantIdAndIsClosed(Long restaurantId, Boolean isClosed);

    // Custom query to check if a restaurant is open at a specific time on a specific day
    @Query("SELECT CASE WHEN COUNT(rh) > 0 THEN true ELSE false END FROM RestaurantHours rh " +
           "WHERE rh.restaurant.id = :restaurantId AND rh.dayOfWeek = :dayOfWeek AND " +
           "rh.isClosed = false AND :time BETWEEN rh.openTime AND rh.closeTime")
    boolean isRestaurantOpenAt(
            @Param("restaurantId") Long restaurantId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time);
}