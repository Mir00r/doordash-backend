# Notification Service

The Notification Service is a comprehensive microservice responsible for managing all types of notifications in the DoorDash-like platform including email, SMS, push notifications, and real-time updates.

## Overview

This service provides a unified notification platform that supports multiple delivery channels and integrates with various third-party providers for maximum reliability and scalability.

## Features

### Notification Channels
- **Email Notifications** - Order confirmations, receipts, marketing campaigns
- **SMS Notifications** - Order updates, delivery alerts, verification codes
- **Push Notifications** - Real-time updates, promotional messages
- **In-App Notifications** - Real-time alerts within the application
- **WebSocket Notifications** - Live updates for drivers and customers

### Core Capabilities
- **Multi-Channel Delivery** - Send notifications through multiple channels simultaneously
- **Template Management** - Dynamic content generation using templates
- **Delivery Tracking** - Track notification delivery status and engagement
- **Retry Mechanisms** - Automatic retry for failed notifications
- **Rate Limiting** - Prevent spam and abuse
- **Personalization** - Customized content based on user preferences
- **Scheduling** - Delayed and scheduled notification delivery
- **Analytics** - Comprehensive delivery and engagement metrics

### Provider Integration
- **Email Providers** - SendGrid, AWS SES, SMTP
- **SMS Providers** - Twilio, AWS SNS
- **Push Notification Providers** - Firebase FCM, Apple APNS
- **Real-time** - WebSocket, Server-Sent Events

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0 with Java 21
- **Database**: PostgreSQL for notification history and preferences
- **Caching**: Redis for rate limiting and temporary storage
- **Messaging**: Apache Kafka for event processing
- **WebSocket**: Spring WebSocket for real-time notifications
- **Security**: Spring Security with JWT authentication
- **Monitoring**: Micrometer with Prometheus
- **Documentation**: OpenAPI 3.0 with Swagger UI

### Key Components
- **Notification Engine** - Core notification processing
- **Template Engine** - Dynamic content generation
- **Provider Adapters** - Integration with external services
- **Delivery Tracker** - Status monitoring and analytics
- **Rate Limiter** - Abuse prevention and throttling
- **Preference Manager** - User notification settings

## API Endpoints

### Notification Management
- `POST /api/v1/notifications/send` - Send immediate notification
- `POST /api/v1/notifications/schedule` - Schedule future notification
- `GET /api/v1/notifications/{id}` - Get notification details
- `GET /api/v1/notifications/user/{userId}` - Get user notifications
- `PUT /api/v1/notifications/{id}/status` - Update notification status

### Template Management
- `POST /api/v1/templates` - Create notification template
- `GET /api/v1/templates` - List all templates
- `PUT /api/v1/templates/{id}` - Update template
- `DELETE /api/v1/templates/{id}` - Delete template

### User Preferences
- `GET /api/v1/preferences/{userId}` - Get user preferences
- `PUT /api/v1/preferences/{userId}` - Update user preferences
- `POST /api/v1/preferences/{userId}/unsubscribe` - Unsubscribe from notifications

### Analytics
- `GET /api/v1/analytics/delivery-stats` - Get delivery statistics
- `GET /api/v1/analytics/engagement` - Get engagement metrics
- `GET /api/v1/analytics/provider-performance` - Provider performance metrics

## Configuration

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=notification_service
DB_USERNAME=notification_user
DB_PASSWORD=your_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Email Providers
SENDGRID_API_KEY=your_sendgrid_key
AWS_SES_ACCESS_KEY=your_aws_key
AWS_SES_SECRET_KEY=your_aws_secret

# SMS Providers
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token

# Push Notification
FIREBASE_CONFIG_PATH=/path/to/firebase-config.json

# JWT Configuration
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400000
```

## Setup and Installation

### Prerequisites
- Java 21
- PostgreSQL 14+
- Redis 6+
- Apache Kafka 3.0+

### Local Development
1. Clone the repository
2. Set up environment variables
3. Run database migrations
4. Start the application

```bash
./gradlew bootRun
```

### Docker Deployment
```bash
docker build -t notification-service .
docker run -p 8084:8084 notification-service
```

## Security

### Authentication
- JWT-based authentication
- Role-based access control
- API key authentication for external integrations

### Data Protection
- Encryption of sensitive data
- PII data anonymization
- Secure storage of provider credentials

### Rate Limiting
- Per-user rate limiting
- Global rate limiting
- Provider-specific throttling

## Monitoring and Observability

### Metrics
- Notification delivery rates
- Provider performance
- Error rates and retry counts
- User engagement metrics

### Health Checks
- Database connectivity
- External provider health
- Queue status monitoring

### Logging
- Structured logging with correlation IDs
- Audit trails for all notifications
- Performance metrics logging

## Testing

### Test Coverage
- Unit tests for business logic
- Integration tests for external providers
- End-to-end notification flow tests
- Performance and load testing

### Test Configuration
```bash
./gradlew test
./gradlew integrationTest
```

## Production Considerations

### Scalability
- Horizontal scaling support
- Queue-based processing
- Provider failover mechanisms
- Load balancing across providers

### Reliability
- Circuit breaker patterns
- Retry mechanisms with exponential backoff
- Dead letter queues for failed notifications
- Provider health monitoring

### Performance
- Async processing for all notifications
- Bulk operations for high-volume sends
- Caching for templates and preferences
- Database optimization

## Integration with Other Services

### Event-Driven Integration
- Listen to order events from Order Service
- Process user events from User Service
- Handle delivery updates from Delivery Service
- Integrate with payment notifications from Payment Service

### API Integration
- User profile data from User Service
- Order details from Order Service
- Restaurant information from Restaurant Service
- Authentication validation from Auth Service

## Compliance

### Privacy Regulations
- GDPR compliance for EU users
- CCPA compliance for California users
- CAN-SPAM compliance for email
- TCPA compliance for SMS

### Data Retention
- Configurable retention policies
- Automatic data purging
- Audit log preservation

## Future Enhancements

### Advanced Features
- Machine learning for send time optimization
- A/B testing for notification content
- Predictive analytics for engagement
- Multi-language support

### Additional Channels
- Voice notifications
- Slack/Teams integration
- Social media notifications
- IoT device notifications

## Contributing

Please follow the coding standards and include tests with your contributions.

## License

This project is proprietary software owned by DoorDash.
