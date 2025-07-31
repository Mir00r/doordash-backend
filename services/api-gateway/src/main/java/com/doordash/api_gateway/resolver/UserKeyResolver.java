package com.doordash.api_gateway.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Key resolver for rate limiting based on authenticated user.
 * Uses the user ID from JWT token as the rate limiting key.
 */
@Slf4j
public class UserKeyResolver implements KeyResolver {

    private static final String ANONYMOUS_USER = "anonymous";
    private static final String USER_ID_CLAIM = "sub";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .cast(Authentication.class)
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .map(JwtAuthenticationToken::getToken)
            .cast(Jwt.class)
            .map(jwt -> {
                String userId = jwt.getClaimAsString(USER_ID_CLAIM);
                if (userId != null) {
                    log.debug("Rate limiting key resolved for user: {}", userId);
                    return "user:" + userId;
                } else {
                    log.debug("No user ID found in JWT, using subject: {}", jwt.getSubject());
                    return "user:" + jwt.getSubject();
                }
            })
            .onErrorResume(throwable -> {
                log.debug("Authentication not found, using anonymous rate limiting");
                return Mono.just(ANONYMOUS_USER);
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.debug("No authentication context, using anonymous rate limiting");
                return Mono.just(ANONYMOUS_USER);
            }));
    }
}
