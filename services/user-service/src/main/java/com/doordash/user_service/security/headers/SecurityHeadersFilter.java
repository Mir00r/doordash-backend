package com.doordash.user_service.security.headers;

import com.doordash.user_service.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Headers Filter for DoorDash User Service.
 * 
 * Implements comprehensive HTTP security headers to protect against common web vulnerabilities
 * following OWASP security guidelines and industry best practices.
 * 
 * Protected Vulnerabilities:
 * - Cross-Site Scripting (XSS)
 * - Clickjacking attacks
 * - MIME type sniffing
 * - Mixed content attacks
 * - Information disclosure
 * - Insecure transport
 * - Content injection
 * 
 * Security Headers Implemented:
 * - X-Frame-Options: Prevents clickjacking attacks
 * - X-Content-Type-Options: Prevents MIME sniffing
 * - X-XSS-Protection: Enables browser XSS filtering
 * - Referrer-Policy: Controls referrer information
 * - Content-Security-Policy: Prevents XSS and injection attacks
 * - Strict-Transport-Security: Enforces HTTPS usage
 * - X-Permitted-Cross-Domain-Policies: Controls Flash/PDF cross-domain access
 * - Feature-Policy/Permissions-Policy: Controls browser feature access
 * 
 * Features:
 * - Configurable security headers
 * - Environment-specific header values
 * - Dynamic CSP policy generation
 * - Integration with Spring Security configuration
 * - Compliance with security standards (OWASP, NIST)
 * - Support for modern browser security features
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    /**
     * Applies security headers to all HTTP responses.
     * 
     * Headers Applied:
     * 1. X-Frame-Options - Clickjacking protection
     * 2. X-Content-Type-Options - MIME sniffing protection
     * 3. X-XSS-Protection - XSS filter activation
     * 4. Referrer-Policy - Referrer information control
     * 5. Content-Security-Policy - Content injection protection
     * 6. Strict-Transport-Security - HTTPS enforcement
     * 7. X-Permitted-Cross-Domain-Policies - Flash/PDF policy
     * 8. Permissions-Policy - Browser feature control
     * 9. Cache-Control - Cache behavior control
     * 10. X-Download-Options - Download behavior control
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O operation fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // Skip if security headers are disabled
        if (!securityProperties.getHeaders().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Apply security headers
            applySecurityHeaders(request, response);
            
            log.debug("Applied security headers to response for: {}", request.getRequestURI());
            
        } catch (Exception e) {
            log.error("Failed to apply security headers: {}", e.getMessage(), e);
            // Continue processing even if header application fails
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Applies all configured security headers to the response.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     */
    private void applySecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        SecurityProperties.Headers config = securityProperties.getHeaders();
        
        // 1. X-Frame-Options - Clickjacking Protection
        applyFrameOptions(response, config);
        
        // 2. X-Content-Type-Options - MIME Sniffing Protection
        applyContentTypeOptions(response, config);
        
        // 3. X-XSS-Protection - XSS Filter
        applyXssProtection(response, config);
        
        // 4. Referrer-Policy - Referrer Information Control
        applyReferrerPolicy(response, config);
        
        // 5. Content-Security-Policy - Content Injection Protection
        applyContentSecurityPolicy(response, config, request);
        
        // 6. Strict-Transport-Security - HTTPS Enforcement
        applyStrictTransportSecurity(response, config, request);
        
        // 7. X-Permitted-Cross-Domain-Policies - Flash/PDF Policy
        applyPermittedCrossDomainPolicies(response);
        
        // 8. Permissions-Policy - Browser Feature Control
        applyPermissionsPolicy(response);
        
        // 9. Cache-Control - Cache Behavior Control
        applyCacheControl(response, request);
        
        // 10. X-Download-Options - Download Behavior Control
        applyDownloadOptions(response);
        
        // 11. X-DNS-Prefetch-Control - DNS Prefetch Control
        applyDnsPrefetchControl(response);
        
        // 12. Expect-CT - Certificate Transparency
        applyExpectCt(response, request);
    }

    /**
     * Applies X-Frame-Options header for clickjacking protection.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     */
    private void applyFrameOptions(HttpServletResponse response, SecurityProperties.Headers config) {
        if (StringUtils.hasText(config.getFrameOptions())) {
            response.setHeader("X-Frame-Options", config.getFrameOptions());
        }
    }

    /**
     * Applies X-Content-Type-Options header for MIME sniffing protection.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     */
    private void applyContentTypeOptions(HttpServletResponse response, SecurityProperties.Headers config) {
        if (StringUtils.hasText(config.getContentTypeOptions())) {
            response.setHeader("X-Content-Type-Options", config.getContentTypeOptions());
        }
    }

    /**
     * Applies X-XSS-Protection header for XSS filter activation.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     */
    private void applyXssProtection(HttpServletResponse response, SecurityProperties.Headers config) {
        if (StringUtils.hasText(config.getXssProtection())) {
            response.setHeader("X-XSS-Protection", config.getXssProtection());
        }
    }

    /**
     * Applies Referrer-Policy header for referrer information control.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     */
    private void applyReferrerPolicy(HttpServletResponse response, SecurityProperties.Headers config) {
        if (StringUtils.hasText(config.getReferrerPolicy())) {
            response.setHeader("Referrer-Policy", config.getReferrerPolicy());
        }
    }

    /**
     * Applies Content-Security-Policy header for content injection protection.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     * @param request the HTTP request for context-aware policy
     */
    private void applyContentSecurityPolicy(
            HttpServletResponse response, 
            SecurityProperties.Headers config, 
            HttpServletRequest request) {
        
        String csp = config.getContentSecurityPolicy();
        if (StringUtils.hasText(csp)) {
            // Dynamic CSP based on request context
            String dynamicCsp = buildDynamicCsp(csp, request);
            response.setHeader("Content-Security-Policy", dynamicCsp);
            
            // Also set report-only header for monitoring
            response.setHeader("Content-Security-Policy-Report-Only", dynamicCsp + "; report-uri /api/v1/security/csp-report");
        }
    }

    /**
     * Builds dynamic Content Security Policy based on request context.
     * 
     * @param baseCsp the base CSP policy
     * @param request the HTTP request
     * @return String the dynamic CSP policy
     */
    private String buildDynamicCsp(String baseCsp, HttpServletRequest request) {
        StringBuilder csp = new StringBuilder(baseCsp);
        
        // Add nonce for inline scripts if needed
        String nonce = generateNonce();
        if (csp.toString().contains("'unsafe-inline'")) {
            csp = new StringBuilder(csp.toString().replace("'unsafe-inline'", "'nonce-" + nonce + "'"));
            request.setAttribute("csp-nonce", nonce);
        }
        
        // Add specific policies for API endpoints
        if (request.getRequestURI().startsWith("/api/")) {
            csp.append("; connect-src 'self' https://api.doordash.com");
        }
        
        return csp.toString();
    }

    /**
     * Generates a cryptographically secure nonce for CSP.
     * 
     * @return String the generated nonce
     */
    private String generateNonce() {
        // Generate a secure random nonce
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);
        return java.util.Base64.getEncoder().encodeToString(nonceBytes);
    }

    /**
     * Applies Strict-Transport-Security header for HTTPS enforcement.
     * 
     * @param response the HTTP response
     * @param config the headers configuration
     * @param request the HTTP request
     */
    private void applyStrictTransportSecurity(
            HttpServletResponse response, 
            SecurityProperties.Headers config, 
            HttpServletRequest request) {
        
        // Only apply HSTS over HTTPS
        if (config.getHsts().isEnabled() && isSecureRequest(request)) {
            StringBuilder hsts = new StringBuilder("max-age=").append(config.getHsts().getMaxAge());
            
            if (config.getHsts().isIncludeSubdomains()) {
                hsts.append("; includeSubDomains");
            }
            
            if (config.getHsts().isPreload()) {
                hsts.append("; preload");
            }
            
            response.setHeader("Strict-Transport-Security", hsts.toString());
        }
    }

    /**
     * Applies X-Permitted-Cross-Domain-Policies header for Flash/PDF policy control.
     * 
     * @param response the HTTP response
     */
    private void applyPermittedCrossDomainPolicies(HttpServletResponse response) {
        // Deny cross-domain policies by default for security
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
    }

    /**
     * Applies Permissions-Policy header for browser feature control.
     * 
     * @param response the HTTP response
     */
    private void applyPermissionsPolicy(HttpServletResponse response) {
        // Restrict potentially dangerous browser features
        String permissionsPolicy = "camera=(), microphone=(), geolocation=(), " +
                                 "payment=(), usb=(), magnetometer=(), gyroscope=(), " +
                                 "accelerometer=(), fullscreen=(self)";
        
        response.setHeader("Permissions-Policy", permissionsPolicy);
    }

    /**
     * Applies Cache-Control header for appropriate cache behavior.
     * 
     * @param response the HTTP response
     * @param request the HTTP request
     */
    private void applyCacheControl(HttpServletResponse response, HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        if (uri.startsWith("/api/") || uri.contains("csrf")) {
            // Prevent caching of API responses and CSRF tokens
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        } else if (uri.startsWith("/static/") || uri.endsWith(".css") || uri.endsWith(".js")) {
            // Allow caching of static resources
            response.setHeader("Cache-Control", "public, max-age=31536000");
        }
    }

    /**
     * Applies X-Download-Options header for download behavior control.
     * 
     * @param response the HTTP response
     */
    private void applyDownloadOptions(HttpServletResponse response) {
        // Prevent IE from executing downloads in the security context of the site
        response.setHeader("X-Download-Options", "noopen");
    }

    /**
     * Applies X-DNS-Prefetch-Control header for DNS prefetch control.
     * 
     * @param response the HTTP response
     */
    private void applyDnsPrefetchControl(HttpServletResponse response) {
        // Control DNS prefetching for privacy and security
        response.setHeader("X-DNS-Prefetch-Control", "off");
    }

    /**
     * Applies Expect-CT header for Certificate Transparency monitoring.
     * 
     * @param response the HTTP response
     * @param request the HTTP request
     */
    private void applyExpectCt(HttpServletResponse response, HttpServletRequest request) {
        // Only apply over HTTPS in production
        if (isSecureRequest(request) && isProductionEnvironment()) {
            response.setHeader("Expect-CT", 
                "max-age=86400, enforce, report-uri=\"https://api.doordash.com/security/ct-report\"");
        }
    }

    /**
     * Checks if the request is made over HTTPS.
     * 
     * @param request the HTTP request
     * @return boolean true if request is secure
     */
    private boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure() || 
               "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto")) ||
               "https".equalsIgnoreCase(request.getScheme());
    }

    /**
     * Checks if the application is running in production environment.
     * 
     * @return boolean true if production environment
     */
    private boolean isProductionEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("prod") || profile.contains("production");
    }
}
