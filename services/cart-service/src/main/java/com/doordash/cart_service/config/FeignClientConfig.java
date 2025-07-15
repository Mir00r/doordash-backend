package com.doordash.cart_service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Forward the JWT token to downstream services if available
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt) {
                
                Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }
}