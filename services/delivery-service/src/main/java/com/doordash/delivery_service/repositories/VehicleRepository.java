package com.doordash.delivery_service.repositories;

import com.doordash.delivery_service.domain.entities.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Vehicle entity operations.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    /**
     * Find vehicle by driver ID
     */
    Optional<Vehicle> findByDriverId(UUID driverId);

    /**
     * Find vehicles by driver ID
     */
    List<Vehicle> findAllByDriverId(UUID driverId);

    /**
     * Find vehicle by license plate
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Find vehicle by VIN
     */
    Optional<Vehicle> findByVin(String vin);

    /**
     * Find vehicles by type
     */
    List<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType);

    /**
     * Find vehicles by status
     */
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    /**
     * Find active vehicles
     */
    List<Vehicle> findByIsActiveTrue();

    /**
     * Find verified vehicles
     */
    List<Vehicle> findByIsVerifiedTrue();

    /**
     * Find vehicles requiring attention
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status IN ('MAINTENANCE', 'INSPECTION_REQUIRED', 'INSURANCE_EXPIRED', 'REGISTRATION_EXPIRED') " +
           "OR v.insuranceExpiryDate <= :currentDate OR v.registrationExpiryDate <= :currentDate OR v.nextInspectionDue <= :currentDate")
    List<Vehicle> findVehiclesRequiringAttention(@Param("currentDate") LocalDate currentDate);

    /**
     * Find vehicles with expiring insurance
     */
    @Query("SELECT v FROM Vehicle v WHERE v.insuranceExpiryDate BETWEEN :currentDate AND :expiryThreshold")
    List<Vehicle> findVehiclesWithExpiringInsurance(@Param("currentDate") LocalDate currentDate, @Param("expiryThreshold") LocalDate expiryThreshold);

    /**
     * Find vehicles with expiring registration
     */
    @Query("SELECT v FROM Vehicle v WHERE v.registrationExpiryDate BETWEEN :currentDate AND :expiryThreshold")
    List<Vehicle> findVehiclesWithExpiringRegistration(@Param("currentDate") LocalDate currentDate, @Param("expiryThreshold") LocalDate expiryThreshold);

    /**
     * Find vehicles needing inspection
     */
    @Query("SELECT v FROM Vehicle v WHERE v.nextInspectionDue BETWEEN :currentDate AND :inspectionThreshold")
    List<Vehicle> findVehiclesNeedingInspection(@Param("currentDate") LocalDate currentDate, @Param("inspectionThreshold") LocalDate inspectionThreshold);

    /**
     * Find available vehicles for delivery
     */
    @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND v.isVerified = true AND v.status = 'ACTIVE'")
    List<Vehicle> findAvailableVehiclesForDelivery();

    /**
     * Find vehicles suitable for delivery type
     */
    @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND v.isVerified = true AND v.status = 'ACTIVE' " +
           "AND ((:deliveryType = 'ALCOHOL' AND v.vehicleType IN ('CAR', 'VAN', 'TRUCK')) OR " +
           "(:deliveryType = 'LARGE_ORDER' AND v.vehicleType IN ('CAR', 'VAN', 'TRUCK')) OR " +
           "(:deliveryType = 'EXPRESS' AND v.vehicleType IN ('MOTORCYCLE', 'SCOOTER', 'BICYCLE')) OR " +
           "(:deliveryType = 'STANDARD'))")
    List<Vehicle> findVehiclesSuitableForDeliveryType(@Param("deliveryType") String deliveryType);

    /**
     * Check if license plate exists
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Check if VIN exists
     */
    boolean existsByVin(String vin);
}
