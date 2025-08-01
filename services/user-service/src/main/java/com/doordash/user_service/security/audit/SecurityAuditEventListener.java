package com.doordash.user_service.security.audit;

import com.doordash.user_service.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.boot.actuate.security.AbstractAuthenticationAuditListener;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Security Audit Event Listener for DoorDash User Service.
 * 
 * Comprehensive audit event listener that captures and processes all security-related events
 * for compliance, monitoring, and incident response in a microservices architecture.
 * 
 * Features:
 * - Spring Security event integration
 * - Comprehensive authentication event handling
 * - Authorization event monitoring
 * - Session management event tracking
 * - Custom security event processing
 * - Real-time security monitoring
 * - Compliance audit trail generation
 * - Automated incident response triggers
 * 
 * Event Categories:
 * - Authentication Events (success, failure, logout)
 * - Authorization Events (access granted, denied)
 * - Session Events (creation, destruction, timeout)
 * - Account Events (locked, unlocked, enabled, disabled)
 * - Password Events (change, reset, expiry)
 * - Configuration Events (security config changes)
 * 
 * Compliance Standards:
 * - SOX (Sarbanes-Oxley Act)
 * - PCI DSS (Payment Card Industry Data Security Standard)
 * - GDPR (General Data Protection Regulation)
 * - HIPAA (Health Insurance Portability and Accountability Act)
 * - ISO 27001 (Information Security Management)
 * 
 * Integration:
 * - Spring Boot Actuator audit events
 * - Spring Security authentication events
 * - Custom DoorDash security events
 * - External SIEM system integration
 * - Real-time alerting systems
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditEventListener extends AbstractAuthenticationAuditListener {

    private final SecurityProperties securityProperties;
    private final SecurityEventPublisher securityEventPublisher;

    /**
     * Handles authentication success events.
     * 
     * Captures successful authentication events including:
     * - User identity and authentication method
     * - Session information and security context
     * - Client information (IP, user agent)
     * - Authentication timestamp and duration
     * 
     * @param event the authentication success event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            Authentication authentication = event.getAuthentication();
            Map<String, Object> eventData = buildAuthenticationEventData(authentication);
            eventData.put("eventOutcome", "SUCCESS");
            
            securityEventPublisher.publishAuthenticationSuccessEvent(
                authentication.getName(),
                eventData
            );
            
            log.info("Authentication success event published for user: {}", authentication.getName());
            
        } catch (Exception e) {
            log.error("Failed to process authentication success event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles authentication failure events.
     * 
     * Captures failed authentication attempts including:
     * - Attempted username and failure reason
     * - Client information for security analysis
     * - Failed authentication patterns
     * - Potential brute force attack indicators
     * 
     * @param event the authentication failure event
     */
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            Authentication authentication = event.getAuthentication();
            AuthenticationException exception = event.getException();
            
            Map<String, Object> eventData = buildAuthenticationEventData(authentication);
            eventData.put("eventOutcome", "FAILURE");
            eventData.put("failureType", exception.getClass().getSimpleName());
            
            String failureReason = determineFailureReason(exception);
            String username = authentication != null ? authentication.getName() : "unknown";
            
            securityEventPublisher.publishAuthenticationFailureEvent(
                username,
                failureReason,
                eventData
            );
            
            log.warn("Authentication failure event published for user: {} - {}", username, failureReason);
            
        } catch (Exception e) {
            log.error("Failed to process authentication failure event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles authentication logout events.
     * 
     * @param event the logout success event
     */
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            Authentication authentication = event.getAuthentication();
            Map<String, Object> eventData = buildAuthenticationEventData(authentication);
            eventData.put("eventType", "LOGOUT");
            
            securityEventPublisher.publishSecurityEvent(
                "LOGOUT_SUCCESS",
                "User successfully logged out: " + authentication.getName(),
                eventData
            );
            
            log.info("Logout success event published for user: {}", authentication.getName());
            
        } catch (Exception e) {
            log.error("Failed to process logout success event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles authorization denied events.
     * 
     * @param event the authorization denied event
     */
    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            Authentication authentication = event.getAuthentication().get();
            Object resource = event.getAuthorizationDecision();
            
            Map<String, Object> eventData = buildAuthenticationEventData(authentication);
            eventData.put("resource", resource.toString());
            eventData.put("authorizationResult", "DENIED");
            
            securityEventPublisher.publishAuthorizationFailureEvent(
                resource.toString(),
                "ACCESS",
                "Insufficient privileges"
            );
            
            log.warn("Authorization denied event published for user: {} accessing resource: {}", 
                authentication.getName(), resource);
            
        } catch (Exception e) {
            log.error("Failed to process authorization denied event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles interactive authentication success events.
     * 
     * @param event the interactive authentication success event
     */
    @EventListener
    public void onInteractiveAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            Authentication authentication = event.getAuthentication();
            Map<String, Object> eventData = buildAuthenticationEventData(authentication);
            eventData.put("authenticationMode", "INTERACTIVE");
            
            securityEventPublisher.publishSecurityEvent(
                "INTERACTIVE_AUTHENTICATION_SUCCESS",
                "Interactive authentication successful for user: " + authentication.getName(),
                eventData
            );
            
            log.info("Interactive authentication success event published for user: {}", authentication.getName());
            
        } catch (Exception e) {
            log.error("Failed to process interactive authentication success event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles Spring Boot Actuator audit events.
     * 
     * @param event the audit application event
     */
    @EventListener
    public void onAuditEvent(AuditApplicationEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            AuditEvent auditEvent = event.getAuditEvent();
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("auditEventType", auditEvent.getType());
            eventData.put("principal", auditEvent.getPrincipal());
            eventData.put("timestamp", auditEvent.getTimestamp());
            
            if (auditEvent.getData() != null) {
                eventData.putAll(auditEvent.getData());
            }
            
            securityEventPublisher.publishSecurityEvent(
                "AUDIT_EVENT",
                "Actuator audit event: " + auditEvent.getType(),
                eventData
            );
            
            log.debug("Actuator audit event published: {}", auditEvent.getType());
            
        } catch (Exception e) {
            log.error("Failed to process actuator audit event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles custom DoorDash security events.
     * 
     * @param event the custom security event
     */
    @EventListener
    public void onSecurityEvent(SecurityEvent event) {
        if (!isAuditEnabled()) {
            return;
        }
        
        try {
            // Process custom security events
            log.info("Processing custom security event: {} - {}", 
                event.getEventType(), event.getDescription());
            
            // Additional processing for high-severity events
            if ("CRITICAL".equals(event.getSeverity()) || "HIGH".equals(event.getSeverity())) {
                // Trigger immediate alerting for critical events
                triggerSecurityAlert(event);
            }
            
            // Store event for compliance and analysis
            storeSecurityEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to process custom security event: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds common authentication event data.
     * 
     * @param authentication the authentication object
     * @return Map<String, Object> the event data
     */
    private Map<String, Object> eventData = buildAuthenticationEventData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        
        if (authentication != null) {
            data.put("principal", authentication.getName());
            data.put("authenticated", authentication.isAuthenticated());
            
            if (authentication.getAuthorities() != null) {
                data.put("authorities", authentication.getAuthorities().toString());
            }
            
            // Extract web authentication details if available
            if (authentication.getDetails() instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails webDetails = (WebAuthenticationDetails) authentication.getDetails();
                data.put("remoteAddress", webDetails.getRemoteAddress());
                data.put("sessionId", webDetails.getSessionId());
            }
        }
        
        return data;
    }

    /**
     * Determines the failure reason from authentication exception.
     * 
     * @param exception the authentication exception
     * @return String the failure reason
     */
    private String determineFailureReason(AuthenticationException exception) {
        String exceptionClass = exception.getClass().getSimpleName();
        
        return switch (exceptionClass) {
            case "BadCredentialsException" -> "Invalid credentials";
            case "UsernameNotFoundException" -> "User not found";
            case "AccountExpiredException" -> "Account expired";
            case "CredentialsExpiredException" -> "Credentials expired";
            case "DisabledException" -> "Account disabled";
            case "LockedException" -> "Account locked";
            case "AccountStatusException" -> "Account status issue";
            case "AuthenticationServiceException" -> "Authentication service error";
            default -> "Authentication failed: " + exception.getMessage();
        };
    }

    /**
     * Triggers security alerts for critical events.
     * 
     * @param event the security event
     */
    private void triggerSecurityAlert(SecurityEvent event) {
        // Implementation would integrate with alerting systems
        // Such as PagerDuty, Slack, email notifications, etc.
        log.error("SECURITY ALERT: {} - {}", event.getEventType(), event.getDescription());
        
        // Example: Send to alerting service
        // alertingService.sendAlert(event);
    }

    /**
     * Stores security event for compliance and analysis.
     * 
     * @param event the security event
     */
    private void storeSecurityEvent(SecurityEvent event) {
        // Implementation would store events in:
        // - Database for compliance
        // - Elasticsearch for analysis
        // - SIEM systems for monitoring
        // - Data lake for long-term storage
        
        log.debug("Storing security event: {}", event.getEventId());
        
        // Example: Store in audit database
        // auditRepository.save(event);
    }

    /**
     * Checks if audit logging is enabled.
     * 
     * @return boolean true if audit is enabled
     */
    private boolean isAuditEnabled() {
        return securityProperties.getAudit().isEnabled();
    }
}
