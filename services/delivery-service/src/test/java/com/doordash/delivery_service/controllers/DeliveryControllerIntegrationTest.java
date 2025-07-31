package com.doordash.delivery_service.controllers;

import com.doordash.delivery_service.domain.dtos.delivery.CreateDeliveryRequest;
import com.doordash.delivery_service.domain.dtos.delivery.DeliveryResponse;
import com.doordash.delivery_service.domain.entities.Delivery;
import com.doordash.delivery_service.repositories.DeliveryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DeliveryController.
 * 
 * Tests the complete flow of delivery operations including creation,
 * assignment, status updates, and completion.
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private CreateDeliveryRequest sampleDeliveryRequest;
    private UUID sampleOrderId;
    private UUID sampleCustomerId;
    private UUID sampleRestaurantId;

    @BeforeEach
    void setUp() {
        sampleOrderId = UUID.randomUUID();
        sampleCustomerId = UUID.randomUUID();
        sampleRestaurantId = UUID.randomUUID();

        // Create sample delivery request
        CreateDeliveryRequest.AddressInfo pickupAddress = CreateDeliveryRequest.AddressInfo.builder()
                .addressLine1("123 Restaurant St")
                .city("San Francisco")
                .state("CA")
                .postalCode("94105")
                .latitude(37.7749)
                .longitude(-122.4194)
                .instructions("Call when you arrive")
                .build();

        CreateDeliveryRequest.AddressInfo deliveryAddress = CreateDeliveryRequest.AddressInfo.builder()
                .addressLine1("456 Customer Ave")
                .addressLine2("Apt 2B")
                .city("San Francisco")
                .state("CA")
                .postalCode("94107")
                .latitude(37.7849)
                .longitude(-122.4094)
                .instructions("Leave at door")
                .build();

        Map<String, Object> specialRequirements = new HashMap<>();
        specialRequirements.put("contactless", true);
        specialRequirements.put("utensils", false);

        sampleDeliveryRequest = CreateDeliveryRequest.builder()
                .orderId(sampleOrderId)
                .customerId(sampleCustomerId)
                .restaurantId(sampleRestaurantId)
                .deliveryType("STANDARD")
                .priority("MEDIUM")
                .pickupAddress(pickupAddress)
                .deliveryAddress(deliveryAddress)
                .requestedDeliveryTime(LocalDateTime.now().plusMinutes(30))
                .specialRequirements(specialRequirements)
                .deliveryInstructions("Ring doorbell")
                .pickupInstructions("Order #12345")
                .customerName("John Doe")
                .customerPhone("+1-555-123-4567")
                .restaurantName("Pizza Palace")
                .restaurantPhone("+1-555-987-6543")
                .orderValue(new BigDecimal("25.99"))
                .deliveryFee(new BigDecimal("4.99"))
                .tip(new BigDecimal("5.00"))
                .paymentMethod("CREDIT_CARD")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDelivery_Success() throws Exception {
        mockMvc.perform(post("/api/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDeliveryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(sampleOrderId.toString())))
                .andExpect(jsonPath("$.customerId", is(sampleCustomerId.toString())))
                .andExpect(jsonPath("$.restaurantId", is(sampleRestaurantId.toString())))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")))
                .andExpect(jsonPath("$.deliveryType", is("STANDARD")))
                .andExpect(jsonPath("$.pickupAddress.addressLine1", is("123 Restaurant St")))
                .andExpected(jsonPath("$.deliveryAddress.addressLine1", is("456 Customer Ave")))
                .andExpected(jsonPath("$.customerName", is("John Doe")))
                .andExpected(jsonPath("$.restaurantName", is("Pizza Palace")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDelivery_InvalidRequest() throws Exception {
        CreateDeliveryRequest invalidRequest = CreateDeliveryRequest.builder()
                .orderId(null) // Missing required field
                .customerId(sampleCustomerId)
                .restaurantId(sampleRestaurantId)
                .deliveryType("INVALID_TYPE")
                .build();

        mockMvc.perform(post("/api/v1/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDeliveryById_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        UUID deliveryId = delivery.getId();

        mockMvc.perform(get("/api/v1/deliveries/{deliveryId}", deliveryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deliveryId.toString())))
                .andExpected(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDeliveryById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/deliveries/{deliveryId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDeliveryByOrderId_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();

        mockMvc.perform(get("/api/v1/deliveries/order/{orderId}", delivery.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(delivery.getOrderId().toString())));
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void testAssignDriverToDelivery_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        UUID deliveryId = delivery.getId();
        UUID driverId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/assign/{driverId}", deliveryId, driverId))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.driverId", is(driverId.toString())))
                .andExpected(jsonPath("$.status", is("ASSIGNED")));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void testUpdateDeliveryStatus_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        UUID deliveryId = delivery.getId();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/status", deliveryId)
                .param("status", "PICKUP_IN_PROGRESS"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.status", is("PICKUP_IN_PROGRESS")));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void testCompletePickup_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        delivery.setStatus(Delivery.DeliveryStatus.PICKUP_IN_PROGRESS);
        deliveryRepository.save(delivery);
        UUID deliveryId = delivery.getId();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/pickup/complete", deliveryId))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.status", is("PICKED_UP")));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void testCompleteDelivery_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        delivery.setStatus(Delivery.DeliveryStatus.EN_ROUTE);
        deliveryRepository.save(delivery);
        UUID deliveryId = delivery.getId();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/complete", deliveryId)
                .param("deliveryProof", "Photo confirmation"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.status", is("DELIVERED")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCancelDelivery_Success() throws Exception {
        // Create a delivery first
        Delivery delivery = createSampleDelivery();
        UUID deliveryId = delivery.getId();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/cancel", deliveryId)
                .param("reason", "Customer changed mind"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testRateDelivery_Success() throws Exception {
        // Create a completed delivery first
        Delivery delivery = createSampleDelivery();
        delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
        deliveryRepository.save(delivery);
        UUID deliveryId = delivery.getId();

        mockMvc.perform(put("/api/v1/deliveries/{deliveryId}/rate", deliveryId)
                .param("rating", "5")
                .param("feedback", "Excellent service!"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.customerRating", is(5)))
                .andExpected(jsonPath("$.customerFeedback", is("Excellent service!")));
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void testGetPendingDeliveries_Success() throws Exception {
        // Create some pending deliveries
        createSampleDelivery();
        createSampleDelivery();

        mockMvc.perform(get("/api/v1/deliveries/pending"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllDeliveries_Success() throws Exception {
        // Create some deliveries
        createSampleDelivery();
        createSampleDelivery();

        mockMvc.perform(get("/api/v1/deliveries")
                .param("page", "0")
                .param("size", "10"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpected(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser(roles = "UNAUTHORIZED")
    void testUnauthorizedAccess_Forbidden() throws Exception {
        UUID deliveryId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/deliveries/{deliveryId}", deliveryId))
                .andExpected(status().isForbidden());
    }

    /**
     * Helper method to create a sample delivery for testing
     */
    private Delivery createSampleDelivery() {
        Delivery delivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .customerId(sampleCustomerId)
                .restaurantId(sampleRestaurantId)
                .deliveryType(Delivery.DeliveryType.STANDARD)
                .priority(Delivery.Priority.MEDIUM)
                .status(Delivery.DeliveryStatus.PENDING)
                .pickupAddressLine1("123 Restaurant St")
                .pickupCity("San Francisco")
                .pickupState("CA")
                .pickupPostalCode("94105")
                .deliveryAddressLine1("456 Customer Ave")
                .deliveryAddressLine2("Apt 2B")
                .deliveryCity("San Francisco")
                .deliveryState("CA")
                .deliveryPostalCode("94107")
                .customerName("John Doe")
                .customerPhone("+1-555-123-4567")
                .restaurantName("Pizza Palace")
                .restaurantPhone("+1-555-987-6543")
                .orderValue(new BigDecimal("25.99"))
                .deliveryFee(new BigDecimal("4.99"))
                .tip(new BigDecimal("5.00"))
                .paymentMethod("CREDIT_CARD")
                .deliveryInstructions("Ring doorbell")
                .pickupInstructions("Order #12345")
                .build();

        return deliveryRepository.save(delivery);
    }
}
