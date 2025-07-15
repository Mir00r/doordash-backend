package com.doordash.ordering_service.services;

import com.doordash.ordering_service.models.dtos.*;
import com.doordash.ordering_service.models.entities.CustomerProfile;

import java.util.UUID;

public interface CustomerProfileService {
    /**
     * Create a new customer profile
     * @param profileRequest the profile request
     * @return the created profile response
     */
    ProfileResponse createProfile(ProfileRequest profileRequest);
    
    /**
     * Get a customer profile by ID
     * @param customerId the customer ID
     * @return the profile response
     */
    ProfileResponse getProfile(UUID customerId);
    
    /**
     * Update a customer profile
     * @param customerId the customer ID
     * @param profileRequest the profile request
     * @return the updated profile response
     */
    ProfileResponse updateProfile(UUID customerId, ProfileRequest profileRequest);
    
    /**
     * Add a delivery address to a customer profile
     * @param customerId the customer ID
     * @param addressRequest the address request
     * @return the delivery address response
     */
    DeliveryAddressResponse addAddress(UUID customerId, AddressRequest addressRequest);
    
    /**
     * Update a delivery address
     * @param customerId the customer ID
     * @param addressId the address ID
     * @param addressRequest the address request
     * @return the updated delivery address response
     */
    DeliveryAddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest addressRequest);
    
    /**
     * Delete a delivery address
     * @param customerId the customer ID
     * @param addressId the address ID
     */
    void deleteAddress(UUID customerId, UUID addressId);
    
    /**
     * Add a payment method to a customer profile
     * @param customerId the customer ID
     * @param paymentMethodRequest the payment method request
     * @return the payment method response
     */
    PaymentMethodResponse addPaymentMethod(UUID customerId, PaymentMethodRequest paymentMethodRequest);
    
    /**
     * Update a payment method
     * @param customerId the customer ID
     * @param paymentMethodId the payment method ID
     * @param paymentMethodRequest the payment method request
     * @return the updated payment method response
     */
    PaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, PaymentMethodRequest paymentMethodRequest);
    
    /**
     * Delete a payment method
     * @param customerId the customer ID
     * @param paymentMethodId the payment method ID
     */
    void deletePaymentMethod(UUID customerId, UUID paymentMethodId);
    
    /**
     * Get a customer profile entity by ID
     * @param customerId the customer ID
     * @return the customer profile entity
     */
    CustomerProfile getCustomerProfileEntity(UUID customerId);
}