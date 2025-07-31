# Payment Service - GitHub Copilot Instructions

## Project Overview
This is the **Payment Service** for the DoorDash-like backend system. It handles secure payment processing, payment method management, refunds, and compliance with PCI DSS standards.

## Architecture & Patterns
- **Microservice Architecture**: Independent service with its own database
- **Clean Architecture**: Domain-driven design with clear separation of concerns
- **Event-Driven Architecture**: Kafka integration for async communication
- **CQRS Pattern**: Separate read/write operations for complex queries
- **Repository Pattern**: Data access abstraction
- **Circuit Breaker Pattern**: Resilience for external payment providers

## Key Technologies
- **Framework**: Spring Boot 3.2 with Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Caching**: Redis for session and payment data
- **Messaging**: Apache Kafka for events
- **Security**: OAuth2 JWT, Spring Security
- **Payment Providers**: Stripe, PayPal, Braintree
- **Monitoring**: Micrometer, Prometheus, Grafana
- **Documentation**: OpenAPI 3.0 (Swagger)

## Domain Entities
- **Payment**: Core payment transaction entity
- **PaymentMethod**: Secure payment method storage (tokenized)
- **Refund**: Refund transactions linked to payments
- **AuditLog**: Comprehensive audit trail for compliance

## Security Requirements
- **PCI DSS Compliance**: Never store sensitive card data
- **Data Encryption**: Encrypt sensitive fields at rest
- **Tokenization**: Use payment provider tokens only
- **HTTPS Only**: All communication must be encrypted
- **JWT Authentication**: Stateless authentication
- **Rate Limiting**: Protect against abuse
- **Input Validation**: Strict validation for all inputs

## Payment Providers Integration
```java
// Example payment processing
@Service
public class StripePaymentProvider implements PaymentProvider {
    // Implement secure payment processing
    // Handle webhooks for status updates
    // Manage tokenization and PCI compliance
}
```

## API Endpoints
- `POST /payments` - Create payment
- `GET /payments/{id}` - Get payment details
- `POST /payments/{id}/process` - Process payment
- `POST /payments/{id}/refunds` - Create refund
- `GET /payment-methods` - List payment methods
- `POST /payment-methods` - Add payment method

## Event Publishing
```java
// Publish payment events to Kafka
@EventListener
public void handlePaymentCompleted(PaymentCompletedEvent event) {
    // Notify order service, delivery service, etc.
}
```

## Error Handling
- Use custom exceptions with proper HTTP status codes
- Include correlation IDs for request tracing
- Never expose sensitive data in error messages
- Log security events for audit

## Testing Strategy
- **Unit Tests**: Service layer with mocked dependencies
- **Integration Tests**: Database and external API interactions
- **Security Tests**: Authentication, authorization, input validation
- **Contract Tests**: API contract validation
- **Load Tests**: Performance under high transaction volume

## Configuration Management
- Environment-specific configurations
- Secure secret management (never commit secrets)
- Feature flags for payment provider selection
- Rate limiting and circuit breaker thresholds

## Monitoring & Observability
- **Metrics**: Payment success rates, response times, error rates
- **Logging**: Structured logging with correlation IDs
- **Tracing**: Distributed tracing for payment flows
- **Alerts**: Critical payment failures, security events

## Compliance & Audit
- **PCI DSS**: Regular compliance assessments
- **Audit Logs**: Complete transaction history
- **Data Retention**: Proper data lifecycle management
- **Privacy**: GDPR/CCPA compliance for user data

## Development Guidelines
1. **Never commit secrets** - Use environment variables
2. **Validate all inputs** - Prevent injection attacks
3. **Use transactions** - Ensure data consistency
4. **Handle failures gracefully** - Implement retry logic
5. **Log security events** - Maintain audit trail
6. **Test payment flows** - Use sandbox environments
7. **Document APIs** - Keep OpenAPI specs updated

## Common Code Patterns
```java
// Service method example
@Transactional
@PreAuthorize("hasRole('USER')")
public PaymentResponse createPayment(PaymentCreateRequest request, UUID userId) {
    validateUser(userId);
    validatePaymentRequest(request);
    
    Payment payment = buildPayment(request, userId);
    Payment savedPayment = paymentRepository.save(payment);
    
    publishPaymentEvent(savedPayment);
    auditLog.logPaymentCreated(savedPayment, userId);
    
    return mapToResponse(savedPayment);
}
```

## Database Conventions
- Use UUIDs for all primary keys
- Include audit fields (created_at, updated_at, version)
- Use JSONB for metadata and flexible data
- Create proper indexes for performance
- Use database constraints for data integrity

## Integration Points
- **Order Service**: Payment status updates
- **User Service**: User authentication and preferences
- **Notification Service**: Payment confirmations
- **Analytics Service**: Payment metrics and reporting
- **Fraud Detection**: Risk scoring and validation

## Performance Considerations
- Cache payment method data in Redis
- Use connection pooling for database
- Implement async processing for non-critical operations
- Monitor and optimize database queries
- Use CDN for static assets

## Deployment & DevOps
- Containerized with Docker
- Kubernetes deployment with health checks
- Blue-green deployment for zero downtime
- Database migrations with Flyway
- Secrets management with Kubernetes secrets

Remember: Security and compliance are paramount in payment processing. Always follow best practices and regulatory requirements.
