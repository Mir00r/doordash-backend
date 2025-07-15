# DoorDash Backend

A microservices-based backend system for a food delivery platform like DoorDash. This system is designed to handle restaurant management, order processing, user management, delivery coordination, payment processing, and notifications.

## System Architecture

The system consists of the following microservices:

1. **API Gateway**: Routes requests to appropriate services and handles cross-cutting concerns like authentication, rate limiting, and logging.

2. **Auth Service**: Manages user authentication, authorization, and token issuance/validation.

3. **Restaurant Service**: Manages restaurant information, menu items, and operating hours.

4. **Order Service**: Handles order creation, processing, and tracking, including shopping cart functionality.

5. **User Service**: Manages user profiles, preferences, and addresses.

6. **Delivery Service**: Coordinates delivery assignments, tracking, and status updates.

7. **Payment Service**: Processes payments and manages payment methods.

8. **Notification Service**: Sends notifications to users, restaurants, and delivery drivers.

## Tech Stack

- **Programming Language**: Java 17
- **Framework**: Spring Boot 3.2.x
- **API Documentation**: OpenAPI (Swagger)
- **Database**: PostgreSQL (for each service)
- **Caching**: Redis
- **Message Broker**: Apache Kafka
- **Service Discovery**: Spring Cloud
- **Security**: OAuth2, JWT
- **Containerization**: Docker
- **Monitoring**: Prometheus, Grafana
- **Distributed Tracing**: OpenTelemetry, Jaeger
- **Database Migration**: Flyway
- **Testing**: JUnit 5, Testcontainers, Mockito

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Gradle

### Running the System

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/doordash-backend.git
   cd doordash-backend
   ```

2. Start the entire system using Docker Compose:
   ```
   docker-compose up -d
   ```

3. The services will be available at the following URLs:
   - API Gateway: http://localhost:8080
   - Auth Service: http://localhost:8081
   - Restaurant Service: http://localhost:8082
   - Order Service: http://localhost:8083
   - User Service: http://localhost:8084
   - Delivery Service: http://localhost:8085
   - Payment Service: http://localhost:8086
   - Notification Service: http://localhost:8087
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000
   - Jaeger: http://localhost:16686

### Running Individual Services

Each service can be run individually using Gradle or Docker. Refer to the README.md file in each service directory for specific instructions.

## API Documentation

Each service provides OpenAPI documentation at `/swagger-ui.html`. For example, to access the Restaurant Service API documentation, visit http://localhost:8082/swagger-ui.html.

## Monitoring

The system includes comprehensive monitoring using Prometheus and Grafana:

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (default credentials: admin/admin)

## Distributed Tracing

Jaeger is used for distributed tracing and is available at http://localhost:16686.

## Service Descriptions

### API Gateway

The API Gateway is the entry point for all client requests. It routes requests to the appropriate services, handles authentication, and provides a unified API for clients.

### Auth Service

The Auth Service handles user authentication and authorization. It issues JWT tokens for authenticated users and validates tokens for protected endpoints.

### Restaurant Service

The Restaurant Service manages restaurant information, menu items, and operating hours. It provides APIs for creating, retrieving, updating, and deleting restaurant data, as well as searching for restaurants based on various criteria.

### Order Service

The Order Service handles order creation, processing, and tracking. It includes shopping cart functionality and integrates with the Payment Service for payment processing.

### User Service

The User Service manages user profiles, preferences, and addresses. It provides APIs for user registration, profile management, and address management.

### Delivery Service

The Delivery Service coordinates delivery assignments, tracking, and status updates. It assigns orders to delivery drivers and tracks the status of deliveries.

### Payment Service

The Payment Service processes payments and manages payment methods. It integrates with external payment gateways to process payments securely.

### Notification Service

The Notification Service sends notifications to users, restaurants, and delivery drivers. It supports various notification channels, including email, SMS, and push notifications.

## Development Guidelines

### Code Style

The project follows the Google Java Style Guide. Code formatting is enforced using the Spotless Gradle plugin.

### Testing

All services should have comprehensive unit and integration tests. Integration tests should use Testcontainers to spin up required dependencies.

### API Design

All APIs should follow RESTful principles and be documented using OpenAPI. API versioning should be included in the URL path (e.g., `/api/v1/restaurants`).

### Error Handling

All services should use consistent error handling and return appropriate HTTP status codes and error messages.

### Logging

All services should use SLF4J for logging and include appropriate log levels. Structured logging should be used to facilitate log aggregation and analysis.

### Security

All services should use OAuth2 and JWT for authentication and authorization. Sensitive data should be encrypted at rest and in transit.
