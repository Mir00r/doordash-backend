# Payment Service

## Overview

The Payment Service is a comprehensive microservice responsible for handling all payment-related operations in the DoorDash-like platform. It provides secure payment processing, fraud detection, refund management, and compliance with PCI DSS standards.

## Features

### Core Payment Operations
- **Payment Processing**: Credit cards, debit cards, digital wallets (Apple Pay, Google Pay, PayPal)
- **Multi-Provider Support**: Stripe, PayPal, Braintree integration
- **Refund Management**: Full and partial refunds with automatic reconciliation
- **Settlement Processing**: Automated payment settlements to restaurants and drivers
- **Subscription Management**: Recurring payments for premium services

### Security & Compliance
- **PCI DSS Compliance**: Secure card data handling and tokenization
- **Fraud Detection**: Real-time fraud detection with machine learning models
- **Data Encryption**: End-to-end encryption for sensitive payment data
- **Audit Logging**: Comprehensive audit trails for all payment operations
- **Rate Limiting**: Protection against payment abuse and attacks

### Financial Features
- **Multi-Currency Support**: International payment processing
- **Split Payments**: Automatic payment distribution to multiple parties
- **Fee Management**: Dynamic fee calculation and commission handling
- **Reconciliation**: Automated financial reconciliation and reporting
- **Chargeback Management**: Dispute handling and chargeback processing

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0 with Java 21
- **Database**: PostgreSQL with encrypted sensitive data storage
- **Caching**: Redis for payment session management
- **Messaging**: Apache Kafka for payment events
- **Security**: Spring Security with OAuth2 and JWT
- **Payment Gateways**: Stripe, PayPal, Braintree
- **Monitoring**: Micrometer with Prometheus

### Core Components
- **Payment Processing Engine**: Handles payment transactions across multiple providers
- **Fraud Detection System**: Real-time fraud analysis and risk scoring
- **Tokenization Service**: Secure card tokenization and vault management
- **Settlement Engine**: Automated payment distribution and reconciliation
- **Audit Service**: Comprehensive logging and compliance reporting

## API Endpoints

### Payment Processing
- `POST /api/v1/payments` - Process payment
- `GET /api/v1/payments/{id}` - Get payment details
- `POST /api/v1/payments/{id}/refund` - Process refund
- `POST /api/v1/payments/{id}/capture` - Capture authorized payment
- `POST /api/v1/payments/{id}/void` - Void payment

### Payment Methods
- `POST /api/v1/payment-methods` - Add payment method
- `GET /api/v1/payment-methods` - List payment methods
- `PUT /api/v1/payment-methods/{id}` - Update payment method
- `DELETE /api/v1/payment-methods/{id}` - Delete payment method

### Subscriptions
- `POST /api/v1/subscriptions` - Create subscription
- `GET /api/v1/subscriptions/{id}` - Get subscription details
- `PUT /api/v1/subscriptions/{id}/cancel` - Cancel subscription

### Analytics & Reporting
- `GET /api/v1/analytics/revenue` - Revenue analytics
- `GET /api/v1/reports/settlements` - Settlement reports
- `GET /api/v1/reports/chargebacks` - Chargeback reports

## Security Features

### PCI DSS Compliance
- Card data tokenization
- Encrypted data storage
- Secure API endpoints
- Regular security audits

### Fraud Detection
- Real-time transaction monitoring
- Machine learning risk scoring
- Device fingerprinting
- Velocity checks

### Data Protection
- Field-level encryption
- TLS 1.3 for data in transit
- Key management with AWS KMS
- Regular key rotation

## Configuration

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payment_service
DB_USERNAME=payment_user
DB_PASSWORD=secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Payment Gateways
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
PAYPAL_CLIENT_ID=...
PAYPAL_CLIENT_SECRET=...

# Encryption
ENCRYPTION_KEY=...
AWS_KMS_KEY_ID=...

# Security
JWT_SECRET=...
JWT_EXPIRATION=3600000
```

### Application Profiles
- `local` - Local development
- `dev` - Development environment
- `staging` - Staging environment
- `prod` - Production environment

## Getting Started

### Prerequisites
- Java 21+
- PostgreSQL 14+
- Redis 6+
- Apache Kafka 3.0+

### Running Locally

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd payment-service
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start dependencies**
   ```bash
   docker-compose up -d postgres redis kafka
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the API**
   - API Documentation: http://localhost:8084/swagger-ui.html
   - Health Check: http://localhost:8084/actuator/health

## Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### Test Coverage
- Unit Tests: Controller, Service, and Repository layers
- Integration Tests: End-to-end API testing
- Security Tests: Authentication and authorization
- Payment Gateway Tests: Mock external payment providers

## Monitoring

### Health Checks
- Database connectivity
- Redis connectivity
- Kafka connectivity
- Payment gateway health
- Encryption service health

### Metrics
- Payment transaction volumes
- Success/failure rates
- Response times
- Fraud detection accuracy
- Revenue metrics

### Alerts
- Failed payment processing
- High fraud detection rates
- Gateway connectivity issues
- Database performance issues

## Deployment

### Docker
```bash
# Build Docker image
docker build -t payment-service:latest .

# Run container
docker run -p 8084:8084 payment-service:latest
```

### Kubernetes
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/
```

## Contributing

1. Follow the existing code style and conventions
2. Write comprehensive tests for new features
3. Update documentation for API changes
4. Ensure security compliance for payment-related changes
5. Follow PCI DSS guidelines for card data handling

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Security

For security concerns or vulnerabilities, please email security@doordash.com

## Support

For technical support, please create an issue in the repository or contact the development team.
