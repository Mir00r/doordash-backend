package com.doordash.ordering_service.controllers;

import com.doordash.ordering_service.models.dtos.*;
import com.doordash.ordering_service.services.CustomerProfileService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Profile", description = "Customer profile management APIs")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @PostMapping
    @Operation(summary = "Create customer profile", description = "Creates a new customer profile")
    @Timed(value = "customer.create_profile", description = "Time taken to create a customer profile")
    public ResponseEntity<CustomerProfileDTO> createCustomerProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCustomerProfileRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CustomerProfileDTO profile = customerProfileService.createCustomerProfile(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get customer profile", description = "Retrieves the customer's profile")
    @Timed(value = "customer.get_profile", description = "Time taken to get a customer profile")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@AuthenticationPrincipal Jwt jwt) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CustomerProfileDTO profile = customerProfileService.getCustomerProfile(customerId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update customer profile", description = "Updates the customer's profile")
    @Timed(value = "customer.update_profile", description = "Time taken to update a customer profile")
    public ResponseEntity<CustomerProfileDTO> updateCustomerProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CustomerProfileDTO profile = customerProfileService.updateCustomerProfile(customerId, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add address", description = "Adds a new address to the customer's profile")
    @Timed(value = "customer.add_address", description = "Time taken to add an address")
    public ResponseEntity<CustomerAddressDTO> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAddressRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CustomerAddressDTO address = customerProfileService.addAddress(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/addresses/{addressId}")
    @Operation(summary = "Update address", description = "Updates an existing address")
    @Timed(value = "customer.update_address", description = "Time taken to update an address")
    public ResponseEntity<CustomerAddressDTO> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        CustomerAddressDTO address = customerProfileService.updateAddress(customerId, addressId, request);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete address", description = "Deletes an address from the customer's profile")
    @Timed(value = "customer.delete_address", description = "Time taken to delete an address")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long addressId) {
        Long customerId = Long.parseLong(jwt.getSubject());
        customerProfileService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/payment-methods")
    @Operation(summary = "Add payment method", description = "Adds a new payment method to the customer's profile")
    @Timed(value = "customer.add_payment_method", description = "Time taken to add a payment method")
    public ResponseEntity<PaymentMethodDTO> addPaymentMethod(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePaymentMethodRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        PaymentMethodDTO paymentMethod = customerProfileService.addPaymentMethod(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethod);
    }

    @PutMapping("/payment-methods/{paymentMethodId}")
    @Operation(summary = "Update payment method", description = "Updates an existing payment method")
    @Timed(value = "customer.update_payment_method", description = "Time taken to update a payment method")
    public ResponseEntity<PaymentMethodDTO> updatePaymentMethod(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long paymentMethodId,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        Long customerId = Long.parseLong(jwt.getSubject());
        PaymentMethodDTO paymentMethod = customerProfileService.updatePaymentMethod(customerId, paymentMethodId, request);
        return ResponseEntity.ok(paymentMethod);
    }

    @DeleteMapping("/payment-methods/{paymentMethodId}")
    @Operation(summary = "Delete payment method", description = "Deletes a payment method from the customer's profile")
    @Timed(value = "customer.delete_payment_method", description = "Time taken to delete a payment method")
    public ResponseEntity<Void> deletePaymentMethod(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long paymentMethodId) {
        Long customerId = Long.parseLong(jwt.getSubject());
        customerProfileService.deletePaymentMethod(customerId, paymentMethodId);
        return ResponseEntity.noContent().build();
    }
}