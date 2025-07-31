# User Service

A comprehensive user management service for the DoorDash backend system, handling user profiles, addresses, preferences, and user-related operations.

## Features

### Core User Management
- User profile management (CRUD operations)
- User preferences and settings
- Account status management
- User search and filtering
- Profile image management

### Address Management
- Multiple address support per user
- Address validation and geocoding
- Default address management
- Address history tracking
- Delivery zone validation

### User Preferences
- Dietary restrictions and allergies
- Cuisine preferences
- Order preferences (delivery/pickup)
- Notification preferences
- Language and timezone settings

### Integration Features
- Seamless integration with Auth Service
- Event-driven communication with other services
- Real-time profile updates
- Audit logging for compliance

## API Endpoints

### User Profile Management
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile
- `DELETE /api/v1/users/profile` - Deactivate user account
- `POST /api/v1/users/profile/avatar` - Upload profile avatar
- `DELETE /api/v1/users/profile/avatar` - Remove profile avatar

### Address Management
- `GET /api/v1/users/addresses` - Get user addresses
- `POST /api/v1/users/addresses` - Add new address
- `PUT /api/v1/users/addresses/{id}` - Update address
- `DELETE /api/v1/users/addresses/{id}` - Delete address
- `PUT /api/v1/users/addresses/{id}/default` - Set default address

### User Preferences
- `GET /api/v1/users/preferences` - Get user preferences
- `PUT /api/v1/users/preferences` - Update user preferences
- `GET /api/v1/users/preferences/dietary` - Get dietary restrictions
- `PUT /api/v1/users/preferences/dietary` - Update dietary restrictions

### Admin Operations
- `GET /api/v1/users` - Search and list users (admin)
- `GET /api/v1/users/{id}` - Get user by ID (admin)
- `PUT /api/v1/users/{id}/status` - Update user status (admin)
- `GET /api/v1/users/analytics` - Get user analytics (admin)

## Architecture

### Domain-Driven Design
- **User Aggregate**: Core user profile and account management
- **Address Aggregate**: Address management with validation
- **Preferences Aggregate**: User preferences and settings
- **Event Sourcing**: Profile change tracking and audit

### Integration Patterns
- **Event-Driven Architecture**: Kafka events for profile changes
- **Circuit Breaker**: Resilient communication with external services
- **Saga Pattern**: Distributed transaction management
- **CQRS**: Separate read/write models for performance

### Security
- OAuth2 resource server integration
- Role-based access control (RBAC)
- Data encryption for PII
- Audit logging for compliance
- Rate limiting and DDoS protection

## Technology Stack

- **Java 21**: Modern language features and performance
- **Spring Boot 3.2**: Enterprise framework
- **PostgreSQL**: Primary data store
- **Redis**: Caching and session management
- **Apache Kafka**: Event streaming
- **AWS S3**: Profile image storage
- **OpenAPI 3**: API documentation
