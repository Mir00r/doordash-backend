package com.doordash.api_gateway.config;

import com.doordash.api_gateway.resolver.IpKeyResolver;
import com.doordash.api_gateway.resolver.UserKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration for rate limiting using Redis.
 * Provides different key resolvers for various rate limiting strategies.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Primary key resolver based on authenticated user
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return new UserKeyResolver();
    }

    /**
     * IP-based key resolver for anonymous requests
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return new IpKeyResolver();
    }

    /**
     * Reactive Redis template for rate limiting operations
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();

        ReactiveRedisTemplate<String, Object> template = 
            new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
        
        return template;
    }

    /**
     * String-based reactive Redis template
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }
}
