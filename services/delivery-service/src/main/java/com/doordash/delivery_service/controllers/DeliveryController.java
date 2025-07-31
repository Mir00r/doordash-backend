package com.doordash.delivery_service.controllers;

import com.doordash.delivery_service.domain.dtos.delivery.CreateDeliveryRequest;
import com.doordash.delivery_service.domain.dtos.delivery.DeliveryResponse;
import com.doordash.delivery_service.domain.entities.Delivery;
import com.doordash.delivery_service.services.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Delivery management operations.
 * 
 * Provides endpoints for delivery creation, assignment, tracking,
 * and completion for order deliveries.
 */
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Management", description = "Operations related to delivery management")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "Create a new delivery", description = "Create a new delivery request for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Delivery created successfully",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Delivery already exists for order")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_SERVICE') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> createDelivery(
            @Valid @RequestBody CreateDeliveryRequest request) {
        log.info("Creating new delivery for order ID: {}", request.getOrderId());
        
        Delivery delivery = mapRequestToEntity(request);
        Delivery savedDelivery = deliveryService.createDelivery(delivery);
        DeliveryResponse response = mapEntityToResponse(savedDelivery);
        
        log.info("Delivery created successfully with ID: {}", savedDelivery.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get delivery by ID", description = "Retrieve delivery information by delivery ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery found",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        log.info("Fetching delivery with ID: {}", deliveryId);
        
        return deliveryService.getDeliveryById(deliveryId)
                .map(this::mapEntityToResponse)
                .map(response -> ResponseEntity.ok(response))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get delivery by order ID", description = "Retrieve delivery information by order ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery found",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('DRIVER') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        log.info("Fetching delivery for order ID: {}", orderId);
        
        return deliveryService.getDeliveryByOrderId(orderId)
                .map(this::mapEntityToResponse)
                .map(response -> ResponseEntity.ok(response))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Assign driver to delivery", description = "Assign a specific driver to a delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver assigned successfully",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Delivery or driver not found"),
        @ApiResponse(responseCode = "400", description = "Driver not available or suitable")
    })
    @PutMapping("/{deliveryId}/assign/{driverId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> assignDriverToDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId,
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Assigning driver {} to delivery {}", driverId, deliveryId);
        
        Delivery assignedDelivery = deliveryService.assignDriverToDelivery(deliveryId, driverId);
        DeliveryResponse response = mapEntityToResponse(assignedDelivery);
        
        log.info("Driver assigned successfully to delivery: {}", deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Auto-assign driver to delivery", description = "Automatically find and assign best available driver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver auto-assigned successfully",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Delivery not found"),
        @ApiResponse(responseCode = "400", description = "No available drivers found")
    })
    @PutMapping("/{deliveryId}/auto-assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> autoAssignDriverToDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        log.info("Auto-assigning driver to delivery: {}", deliveryId);
        
        Delivery assignedDelivery = deliveryService.autoAssignDriverToDelivery(deliveryId);
        DeliveryResponse response = mapEntityToResponse(assignedDelivery);
        
        log.info("Driver auto-assigned successfully to delivery: {}", deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update delivery status", description = "Update the status of a delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully",
                content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Delivery not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition")
    })
    @PutMapping("/{deliveryId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER') or hasRole('DISPATCHER')")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId,
            @Parameter(description = "New Status") @RequestParam String status) {
        log.info("Updating delivery {} status to: {}", deliveryId, status);
        
        Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        Delivery updatedDelivery = deliveryService.updateDeliveryStatus(deliveryId, deliveryStatus);
        DeliveryResponse response = mapEntityToResponse(updatedDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Start pickup process", description = "Mark delivery as pickup in progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pickup started successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @PutMapping("/{deliveryId}/pickup/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<DeliveryResponse> startPickup(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        log.info("Starting pickup for delivery: {}", deliveryId);
        
        Delivery updatedDelivery = deliveryService.startPickup(deliveryId);
        DeliveryResponse response = mapEntityToResponse(updatedDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete pickup", description = "Mark pickup as completed and start delivery")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pickup completed successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @PutMapping("/{deliveryId}/pickup/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<DeliveryResponse> completePickup(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId) {
        log.info("Completing pickup for delivery: {}", deliveryId);
        
        Delivery updatedDelivery = deliveryService.completePickup(deliveryId);
        DeliveryResponse response = mapEntityToResponse(updatedDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete delivery", description = "Mark delivery as completed with proof")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery completed successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @PutMapping("/{deliveryId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<DeliveryResponse> completeDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId,
            @Parameter(description = "Delivery Proof") @RequestParam(required = false) String deliveryProof) {
        log.info("Completing delivery: {}", deliveryId);
        
        Delivery completedDelivery = deliveryService.completeDelivery(deliveryId, deliveryProof);
        DeliveryResponse response = mapEntityToResponse(completedDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel delivery", description = "Cancel a delivery with reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @PutMapping("/{deliveryId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('RESTAURANT_MANAGER')")
    public ResponseEntity<DeliveryResponse> cancelDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId,
            @Parameter(description = "Cancellation Reason") @RequestParam String reason) {
        log.info("Cancelling delivery {} with reason: {}", deliveryId, reason);
        
        Delivery cancelledDelivery = deliveryService.cancelDelivery(deliveryId, reason);
        DeliveryResponse response = mapEntityToResponse(cancelledDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rate delivery", description = "Rate delivery and provide feedback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery rated successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    @PutMapping("/{deliveryId}/rate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<DeliveryResponse> rateDelivery(
            @Parameter(description = "Delivery ID") @PathVariable UUID deliveryId,
            @Parameter(description = "Rating (1-5)") @RequestParam Integer rating,
            @Parameter(description = "Feedback") @RequestParam(required = false) String feedback) {
        log.info("Rating delivery {} with rating: {}", deliveryId, rating);
        
        Delivery ratedDelivery = deliveryService.rateDelivery(deliveryId, rating, feedback);
        DeliveryResponse response = mapEntityToResponse(ratedDelivery);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get deliveries by customer", description = "Get all deliveries for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully")
    })
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @customerAuthService.isCurrentCustomer(#customerId))")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {
        log.info("Fetching deliveries for customer: {}", customerId);
        
        List<Delivery> deliveries = deliveryService.getDeliveriesByCustomer(customerId);
        List<DeliveryResponse> response = deliveries.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get deliveries by driver", description = "Get all deliveries for a specific driver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully")
    })
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        
        List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        List<DeliveryResponse> response = deliveries.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending deliveries", description = "Get all pending deliveries waiting for assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending deliveries retrieved successfully")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<List<DeliveryResponse>> getPendingDeliveries() {
        log.info("Fetching pending deliveries");
        
        List<Delivery> deliveries = deliveryService.getPendingDeliveries();
        List<DeliveryResponse> response = deliveries.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get overdue deliveries", description = "Get all overdue deliveries")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue deliveries retrieved successfully")
    })
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<List<DeliveryResponse>> getOverdueDeliveries() {
        log.info("Fetching overdue deliveries");
        
        List<Delivery> deliveries = deliveryService.getOverdueDeliveries();
        List<DeliveryResponse> response = deliveries.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all deliveries", description = "Get all deliveries with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER')")
    public ResponseEntity<Page<DeliveryResponse>> getAllDeliveries(Pageable pageable) {
        log.info("Fetching all deliveries with pagination: {}", pageable);
        
        Page<Delivery> deliveries = deliveryService.getAllDeliveries(pageable);
        Page<DeliveryResponse> response = deliveries.map(this::mapEntityToResponse);
        
        return ResponseEntity.ok(response);
    }

    // Utility methods for mapping between DTOs and entities
    private Delivery mapRequestToEntity(CreateDeliveryRequest request) {
        return Delivery.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .deliveryZoneId(request.getDeliveryZoneId())
                .deliveryType(Delivery.DeliveryType.valueOf(request.getDeliveryType().toUpperCase()))
                .priority(Delivery.Priority.valueOf(request.getPriority().toUpperCase()))
                .pickupAddressLine1(request.getPickupAddress().getAddressLine1())
                .pickupAddressLine2(request.getPickupAddress().getAddressLine2())
                .pickupCity(request.getPickupAddress().getCity())
                .pickupState(request.getPickupAddress().getState())
                .pickupPostalCode(request.getPickupAddress().getPostalCode())
                .deliveryAddressLine1(request.getDeliveryAddress().getAddressLine1())
                .deliveryAddressLine2(request.getDeliveryAddress().getAddressLine2())
                .deliveryCity(request.getDeliveryAddress().getCity())
                .deliveryState(request.getDeliveryAddress().getState())
                .deliveryPostalCode(request.getDeliveryAddress().getPostalCode())
                .requestedDeliveryTime(request.getRequestedDeliveryTime())
                .specialRequirements(request.getSpecialRequirements())
                .deliveryInstructions(request.getDeliveryInstructions())
                .pickupInstructions(request.getPickupInstructions())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .restaurantName(request.getRestaurantName())
                .restaurantPhone(request.getRestaurantPhone())
                .orderValue(request.getOrderValue())
                .deliveryFee(request.getDeliveryFee())
                .tip(request.getTip())
                .paymentMethod(request.getPaymentMethod())
                .status(Delivery.DeliveryStatus.PENDING)
                .build();
    }

    private DeliveryResponse mapEntityToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .customerId(delivery.getCustomerId())
                .restaurantId(delivery.getRestaurantId())
                .driverId(delivery.getDriverId())
                .deliveryZoneId(delivery.getDeliveryZoneId())
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .priority(delivery.getPriority() != null ? delivery.getPriority().name() : null)
                .deliveryType(delivery.getDeliveryType() != null ? delivery.getDeliveryType().name() : null)
                .pickupAddress(DeliveryResponse.AddressInfo.builder()
                        .addressLine1(delivery.getPickupAddressLine1())
                        .addressLine2(delivery.getPickupAddressLine2())
                        .city(delivery.getPickupCity())
                        .state(delivery.getPickupState())
                        .postalCode(delivery.getPickupPostalCode())
                        .latitude(delivery.getPickupLocation() != null ? delivery.getPickupLocation().getY() : null)
                        .longitude(delivery.getPickupLocation() != null ? delivery.getPickupLocation().getX() : null)
                        .instructions(delivery.getPickupInstructions())
                        .build())
                .deliveryAddress(DeliveryResponse.AddressInfo.builder()
                        .addressLine1(delivery.getDeliveryAddressLine1())
                        .addressLine2(delivery.getDeliveryAddressLine2())
                        .city(delivery.getDeliveryCity())
                        .state(delivery.getDeliveryState())
                        .postalCode(delivery.getDeliveryPostalCode())
                        .latitude(delivery.getDeliveryLocation() != null ? delivery.getDeliveryLocation().getY() : null)
                        .longitude(delivery.getDeliveryLocation() != null ? delivery.getDeliveryLocation().getX() : null)
                        .instructions(delivery.getDeliveryInstructions())
                        .build())
                .requestedDeliveryTime(delivery.getRequestedDeliveryTime())
                .estimatedPickupTime(delivery.getEstimatedPickupTime())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .actualPickupTime(delivery.getPickupCompletedTime())
                .actualDeliveryTime(delivery.getActualDeliveryTime())
                .deliveryFee(delivery.getDeliveryFee())
                .driverPayout(delivery.getDriverPayout())
                .tip(delivery.getTip())
                .customerRating(delivery.getCustomerRating())
                .customerFeedback(delivery.getCustomerFeedback())
                .driverRating(delivery.getDriverRating())
                .driverFeedback(delivery.getDriverFeedback())
                .specialRequirements(delivery.getSpecialRequirements())
                .deliveryInstructions(delivery.getDeliveryInstructions())
                .pickupInstructions(delivery.getPickupInstructions())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }
}
