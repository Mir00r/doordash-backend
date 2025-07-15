package com.doordash.restaurant_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String phoneNumber;

    private String email;

    private String website;

    @Column(nullable = false)
    private String cuisine;

    @Column(nullable = false)
    private BigDecimal averageRating;

    private Integer totalRatings;

    @Column(nullable = false)
    private BigDecimal deliveryFee;

    @Column(nullable = false)
    private Integer estimatedDeliveryTime; // in minutes

    @Column(nullable = false)
    private Boolean isOpen;

    private String imageUrl;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantHours> hours = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to add a menu item
    public void addMenuItem(MenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setRestaurant(this);
    }

    // Helper method to remove a menu item
    public void removeMenuItem(MenuItem menuItem) {
        menuItems.remove(menuItem);
        menuItem.setRestaurant(null);
    }

    // Helper method to add restaurant hours
    public void addHours(RestaurantHours restaurantHours) {
        hours.add(restaurantHours);
        restaurantHours.setRestaurant(this);
    }
}