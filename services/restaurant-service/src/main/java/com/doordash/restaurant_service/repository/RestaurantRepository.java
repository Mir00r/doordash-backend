package com.doordash.restaurant_service.repository;

import com.doordash.restaurant_service.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find restaurants by cuisine
    Page<Restaurant> findByCuisineIgnoreCase(String cuisine, Pageable pageable);

    // Find open restaurants
    Page<Restaurant> findByIsOpenTrue(Pageable pageable);

    // Find restaurants by name containing the search term
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find restaurants by city
    Page<Restaurant> findByCityIgnoreCase(String city, Pageable pageable);

    // Find restaurants by zip code
    Page<Restaurant> findByZipCode(String zipCode, Pageable pageable);

    // Find top rated restaurants
    Page<Restaurant> findByAverageRatingGreaterThanEqual(Double rating, Pageable pageable);

    // Custom query to search restaurants by multiple criteria
    @Query("SELECT r FROM Restaurant r WHERE " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:cuisine IS NULL OR LOWER(r.cuisine) = LOWER(:cuisine)) AND " +
           "(:city IS NULL OR LOWER(r.city) = LOWER(:city)) AND " +
           "(:zipCode IS NULL OR r.zipCode = :zipCode) AND " +
           "(:isOpen IS NULL OR r.isOpen = :isOpen)")
    Page<Restaurant> searchRestaurants(
            @Param("name") String name,
            @Param("cuisine") String cuisine,
            @Param("city") String city,
            @Param("zipCode") String zipCode,
            @Param("isOpen") Boolean isOpen,
            Pageable pageable);
}