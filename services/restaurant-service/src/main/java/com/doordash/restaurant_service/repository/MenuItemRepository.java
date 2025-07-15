package com.doordash.restaurant_service.repository;

import com.doordash.restaurant_service.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Find menu items by restaurant id
    List<MenuItem> findByRestaurantId(Long restaurantId);

    // Find menu items by restaurant id with pagination
    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    // Find menu items by category and restaurant id
    List<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category);

    // Find available menu items by restaurant id
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    // Find menu items by name containing the search term and restaurant id
    List<MenuItem> findByRestaurantIdAndNameContainingIgnoreCase(Long restaurantId, String name);

    // Find vegetarian menu items by restaurant id
    List<MenuItem> findByRestaurantIdAndVegetarianTrue(Long restaurantId);

    // Find vegan menu items by restaurant id
    List<MenuItem> findByRestaurantIdAndVeganTrue(Long restaurantId);

    // Find gluten-free menu items by restaurant id
    List<MenuItem> findByRestaurantIdAndGlutenFreeTrue(Long restaurantId);

    // Find spicy menu items by restaurant id
    List<MenuItem> findByRestaurantIdAndSpicyTrue(Long restaurantId);

    // Custom query to search menu items by multiple criteria
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(m.category) = LOWER(:category)) AND " +
           "(:available IS NULL OR m.available = :available) AND " +
           "(:vegetarian IS NULL OR m.vegetarian = :vegetarian) AND " +
           "(:vegan IS NULL OR m.vegan = :vegan) AND " +
           "(:glutenFree IS NULL OR m.glutenFree = :glutenFree) AND " +
           "(:spicy IS NULL OR m.spicy = :spicy)")
    Page<MenuItem> searchMenuItems(
            @Param("restaurantId") Long restaurantId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("available") Boolean available,
            @Param("vegetarian") Boolean vegetarian,
            @Param("vegan") Boolean vegan,
            @Param("glutenFree") Boolean glutenFree,
            @Param("spicy") Boolean spicy,
            Pageable pageable);
}