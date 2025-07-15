package com.doordash.ordering_service.services.impl;

import com.doordash.ordering_service.exceptions.CustomerNotFoundException;
import com.doordash.ordering_service.exceptions.ResourceNotFoundException;
import com.doordash.ordering_service.models.dtos.*;
import com.doordash.ordering_service.models.entities.CustomerAddress;
import com.doordash.ordering_service.models.entities.CustomerProfile;
import com.doordash.ordering_service.models.entities.PaymentMethod;
import com.doordash.ordering_service.repositories.CustomerAddressRepository;
import com.doordash.ordering_service.repositories.CustomerProfileRepository;
import com.doordash.ordering_service.repositories.PaymentMethodRepository;
import com.doordash.ordering_service.services.CustomerProfileService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerProfileRepository profileRepository;
    private final CustomerAddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
        log.info("Creating customer profile with email: {}", profileRequest.getEmail());
        meterRegistry.counter("customer.profile.create").increment();
        
        // Check if email already exists
        if (profileRepository.existsByEmail(profileRequest.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists: " + profileRequest.getEmail());
        }
        
        CustomerProfile profile = CustomerProfile.builder()
                .name(profileRequest.getName())
                .email(profileRequest.getEmail())
                .phone(profileRequest.getPhone())
                .build();
        
        profile = profileRepository.save(profile);
        
        return mapToProfileResponse(profile, List.of(), List.of());
    }

    @Override
    @Cacheable(value = "customerProfile", key = "#customerId")
    public ProfileResponse getProfile(UUID customerId) {
        log.info("Getting customer profile for ID: {}", customerId);
        meterRegistry.counter("customer.profile.get").increment();
        
        CustomerProfile profile = getCustomerProfileEntity(customerId);
        List<CustomerAddress> addresses = addressRepository.findByCustomerId(customerId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByCustomerId(customerId);
        
        return mapToProfileResponse(profile, addresses, paymentMethods);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public ProfileResponse updateProfile(UUID customerId, ProfileRequest profileRequest) {
        log.info("Updating customer profile for ID: {}", customerId);
        meterRegistry.counter("customer.profile.update").increment();
        
        CustomerProfile profile = getCustomerProfileEntity(customerId);
        
        // Check if email is being changed and already exists
        if (!profile.getEmail().equals(profileRequest.getEmail()) && 
                profileRepository.existsByEmail(profileRequest.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists: " + profileRequest.getEmail());
        }
        
        profile.setName(profileRequest.getName());
        profile.setEmail(profileRequest.getEmail());
        profile.setPhone(profileRequest.getPhone());
        
        profile = profileRepository.save(profile);
        
        List<CustomerAddress> addresses = addressRepository.findByCustomerId(customerId);
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByCustomerId(customerId);
        
        return mapToProfileResponse(profile, addresses, paymentMethods);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public DeliveryAddressResponse addAddress(UUID customerId, AddressRequest addressRequest) {
        log.info("Adding address for customer ID: {}", customerId);
        meterRegistry.counter("customer.address.add").increment();
        
        // Verify customer exists
        getCustomerProfileEntity(customerId);
        
        CustomerAddress address = CustomerAddress.builder()
                .customerId(customerId)
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .zipCode(addressRequest.getZipCode())
                .country(addressRequest.getCountry())
                .latitude(addressRequest.getLatitude())
                .longitude(addressRequest.getLongitude())
                .isDefault(addressRequest.getIsDefault())
                .build();
        
        // If this is the default address, unset any existing default
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.unsetDefaultAddresses(customerId, UUID.randomUUID()); // Using random UUID since address ID is not yet assigned
        }
        
        address = addressRepository.save(address);
        
        // If this is the first address, make it default
        if (addressRepository.findByCustomerId(customerId).size() == 1) {
            address.setIsDefault(true);
            address = addressRepository.save(address);
        }
        
        return mapToDeliveryAddressResponse(address);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public DeliveryAddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest addressRequest) {
        log.info("Updating address ID: {} for customer ID: {}", addressId, customerId);
        meterRegistry.counter("customer.address.update").increment();
        
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Verify address belongs to customer
        if (!address.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressId + " for customer: " + customerId);
        }
        
        address.setAddressLine1(addressRequest.getAddressLine1());
        address.setAddressLine2(addressRequest.getAddressLine2());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZipCode(addressRequest.getZipCode());
        address.setCountry(addressRequest.getCountry());
        address.setLatitude(addressRequest.getLatitude());
        address.setLongitude(addressRequest.getLongitude());
        
        // If this is being set as default, unset any existing default
        if (Boolean.TRUE.equals(addressRequest.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.unsetDefaultAddresses(customerId, addressId);
            address.setIsDefault(true);
        }
        
        address = addressRepository.save(address);
        
        return mapToDeliveryAddressResponse(address);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public void deleteAddress(UUID customerId, UUID addressId) {
        log.info("Deleting address ID: {} for customer ID: {}", addressId, customerId);
        meterRegistry.counter("customer.address.delete").increment();
        
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Verify address belongs to customer
        if (!address.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressId + " for customer: " + customerId);
        }
        
        addressRepository.delete(address);
        
        // If this was the default address, set another address as default if available
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<CustomerAddress> addresses = addressRepository.findByCustomerId(customerId);
            if (!addresses.isEmpty()) {
                CustomerAddress newDefault = addresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public PaymentMethodResponse addPaymentMethod(UUID customerId, PaymentMethodRequest paymentMethodRequest) {
        log.info("Adding payment method for customer ID: {}", customerId);
        meterRegistry.counter("customer.payment_method.add").increment();
        
        // Verify customer exists
        getCustomerProfileEntity(customerId);
        
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .customerId(customerId)
                .paymentProvider(paymentMethodRequest.getPaymentProvider())
                .paymentToken(paymentMethodRequest.getPaymentToken())
                .cardLastFour(paymentMethodRequest.getCardLastFour())
                .cardType(paymentMethodRequest.getCardType())
                .expiryMonth(paymentMethodRequest.getExpiryMonth())
                .expiryYear(paymentMethodRequest.getExpiryYear())
                .isDefault(paymentMethodRequest.getIsDefault())
                .build();
        
        // If this is the default payment method, unset any existing default
        if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            paymentMethodRepository.unsetDefaultPaymentMethods(customerId, UUID.randomUUID()); // Using random UUID since payment method ID is not yet assigned
        }
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        // If this is the first payment method, make it default
        if (paymentMethodRepository.findByCustomerId(customerId).size() == 1) {
            paymentMethod.setIsDefault(true);
            paymentMethod = paymentMethodRepository.save(paymentMethod);
        }
        
        return mapToPaymentMethodResponse(paymentMethod);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public PaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, PaymentMethodRequest paymentMethodRequest) {
        log.info("Updating payment method ID: {} for customer ID: {}", paymentMethodId, customerId);
        meterRegistry.counter("customer.payment_method.update").increment();
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId));
        
        // Verify payment method belongs to customer
        if (!paymentMethod.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId + " for customer: " + customerId);
        }
        
        paymentMethod.setPaymentProvider(paymentMethodRequest.getPaymentProvider());
        paymentMethod.setCardLastFour(paymentMethodRequest.getCardLastFour());
        paymentMethod.setCardType(paymentMethodRequest.getCardType());
        paymentMethod.setExpiryMonth(paymentMethodRequest.getExpiryMonth());
        paymentMethod.setExpiryYear(paymentMethodRequest.getExpiryYear());
        
        // If this is being set as default, unset any existing default
        if (Boolean.TRUE.equals(paymentMethodRequest.getIsDefault()) && !Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            paymentMethodRepository.unsetDefaultPaymentMethods(customerId, paymentMethodId);
            paymentMethod.setIsDefault(true);
        }
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        return mapToPaymentMethodResponse(paymentMethod);
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerProfile", key = "#customerId")
    public void deletePaymentMethod(UUID customerId, UUID paymentMethodId) {
        log.info("Deleting payment method ID: {} for customer ID: {}", paymentMethodId, customerId);
        meterRegistry.counter("customer.payment_method.delete").increment();
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId));
        
        // Verify payment method belongs to customer
        if (!paymentMethod.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId + " for customer: " + customerId);
        }
        
        paymentMethodRepository.delete(paymentMethod);
        
        // If this was the default payment method, set another payment method as default if available
        if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            List<PaymentMethod> paymentMethods = paymentMethodRepository.findByCustomerId(customerId);
            if (!paymentMethods.isEmpty()) {
                PaymentMethod newDefault = paymentMethods.get(0);
                newDefault.setIsDefault(true);
                paymentMethodRepository.save(newDefault);
            }
        }
    }

    @Override
    public CustomerProfile getCustomerProfileEntity(UUID customerId) {
        return profileRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }
    
    private ProfileResponse mapToProfileResponse(CustomerProfile profile, List<CustomerAddress> addresses, List<PaymentMethod> paymentMethods) {
        List<DeliveryAddressResponse> addressResponses = addresses.stream()
                .map(this::mapToDeliveryAddressResponse)
                .collect(Collectors.toList());
        
        List<PaymentMethodResponse> paymentMethodResponses = paymentMethods.stream()
                .map(this::mapToPaymentMethodResponse)
                .collect(Collectors.toList());
        
        return ProfileResponse.builder()
                .customerId(profile.getId())
                .name(profile.getName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .addresses(addressResponses)
                .paymentMethods(paymentMethodResponses)
                .build();
    }
    
    private DeliveryAddressResponse mapToDeliveryAddressResponse(CustomerAddress address) {
        return DeliveryAddressResponse.builder()
                .id(address.getId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .build();
    }
    
    private PaymentMethodResponse mapToPaymentMethodResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .paymentProvider(paymentMethod.getPaymentProvider())
                .cardLastFour(paymentMethod.getCardLastFour())
                .cardType(paymentMethod.getCardType())
                .expiryMonth(paymentMethod.getExpiryMonth())
                .expiryYear(paymentMethod.getExpiryYear())
                .isDefault(paymentMethod.getIsDefault())
                .build();
    }
}