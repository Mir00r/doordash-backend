package com.doordash.api_gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom gateway filter factory for JWT authentication.
 * Adds user information headers to downstream requests.
 */
@Component
@Slf4j
public class JwtAuthenticationGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                .cast(SecurityContext.class)
                .map(SecurityContext::getAuthentication)
                .cast(Authentication.class)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .cast(Jwt.class)
                .flatMap(jwt -> {
                    ServerWebExchange modifiedExchange = addUserHeaders(exchange, jwt);
                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
        };
    }

    /**
     * Add user information headers to the request for downstream services.
     */
    private ServerWebExchange addUserHeaders(ServerWebExchange exchange, Jwt jwt) {
        return exchange.mutate()
            .request(request -> request
                .header("X-User-Id", jwt.getSubject())
                .header("X-User-Email", jwt.getClaimAsString("email"))
                .header("X-User-Roles", String.join(",", jwt.getClaimAsStringList("roles")))
                .header("X-User-Name", jwt.getClaimAsString("preferred_username"))
                .header("X-Token-Issued-At", String.valueOf(jwt.getIssuedAt().getEpochSecond()))
                .header("X-Token-Expires-At", String.valueOf(jwt.getExpiresAt().getEpochSecond()))
            )
            .build();
    }

    @Data
    public static class Config {
        private String headerPrefix = "X-User-";
        private boolean includeTokenInfo = true;
        private boolean includeRoles = true;
    }
}
