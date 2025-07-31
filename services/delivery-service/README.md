# Delivery Service

The Delivery Service is a comprehensive microservice that handles all delivery-related operations for the DoorDash platform, including driver management, delivery tracking, route optimization, and real-time updates.

## Features

### Core Functionality
- **Driver Management**: Driver registration, verification, and profile management
- **Delivery Tracking**: Real-time delivery tracking and status updates
- **Route Optimization**: Intelligent routing for efficient deliveries
- **Real-time Communication**: WebSocket-based real-time updates
- **Delivery Analytics**: Performance metrics and insights
- **Driver Earnings**: Earnings calculation and payout management

### Advanced Features
- **Geofencing**: Location-based triggers and notifications
- **Dynamic Pricing**: Surge pricing and demand-based adjustments
- **Fleet Management**: Vehicle management and maintenance tracking
- **Driver Ratings**: Customer and restaurant rating system
- **Delivery Zones**: Geographic service area management
- **Emergency Protocols**: Safety and emergency response features

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: Java 21
- **Database**: PostgreSQL 15 with PostGIS for geospatial data
- **Caching**: Redis 7 with Redisson
- **Message Broker**: Apache Kafka
- **Real-time Communication**: WebSocket
- **Maps & Routing**: Google Maps API
- **File Storage**: AWS S3
- **Monitoring**: Micrometer with Prometheus
- **Documentation**: OpenAPI 3 with Swagger UI

## Architecture

### Domain Models
- **Driver**: Driver profiles and information
- **Vehicle**: Vehicle registration and details
- **Delivery**: Delivery requests and tracking
- **Route**: Optimized delivery routes
- **DeliveryTracking**: Real-time location and status updates
- **DriverEarnings**: Earnings and payout tracking
- **DeliveryZone**: Service area definitions

### API Endpoints

#### Driver Management
- `POST /api/v1/drivers` - Register new driver
- `GET /api/v1/drivers/{driverId}` - Get driver profile
- `PUT /api/v1/drivers/{driverId}` - Update driver profile
- `POST /api/v1/drivers/{driverId}/documents` - Upload verification documents
- `GET /api/v1/drivers/{driverId}/earnings` - Get driver earnings

#### Delivery Operations
- `POST /api/v1/deliveries` - Create delivery request
- `GET /api/v1/deliveries/{deliveryId}` - Get delivery details
- `PUT /api/v1/deliveries/{deliveryId}/status` - Update delivery status
- `POST /api/v1/deliveries/{deliveryId}/assign` - Assign driver to delivery
- `GET /api/v1/deliveries/{deliveryId}/tracking` - Get real-time tracking

#### Route Optimization
- `POST /api/v1/routes/optimize` - Optimize delivery routes
- `GET /api/v1/routes/{routeId}` - Get route details
- `PUT /api/v1/routes/{routeId}/update` - Update route progress

#### Real-time Updates
- `WebSocket /ws/delivery/{deliveryId}` - Real-time delivery updates
- `WebSocket /ws/driver/{driverId}` - Driver location updates

## Security

- **JWT Authentication**: Secure API access with role-based permissions
- **Role-based Access Control**: DRIVER, CUSTOMER, ADMIN, DISPATCHER roles
- **Data Encryption**: Sensitive data protection
- **Location Privacy**: Driver location data protection
- **Audit Logging**: Comprehensive activity tracking

## Integration

### External Services
- **Auth Service**: User authentication and authorization
- **User Service**: Customer and driver profile information
- **Order Service**: Order details and delivery requirements
- **Restaurant Service**: Pickup location and restaurant information
- **Google Maps API**: Geocoding, routing, and ETA calculations

### Event-Driven Communication
- **Delivery Events**: Created, assigned, picked_up, delivered, cancelled
- **Driver Events**: Available, busy, offline, location_updated
- **Order Events**: Ready for pickup, delivery assigned
- **Notification Events**: SMS, push notifications for status updates

## Development Setup

### Prerequisites
- Java 21
- Docker and Docker Compose
- PostgreSQL 15 with PostGIS extension
- Redis 7
- Apache Kafka

### Local Development
```bash
# Clone the repository
git clone <repository-url>
cd delivery-service

# Start dependencies
docker-compose up -d postgres redis kafka

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Build Docker image
docker build -t delivery-service .
```

### Environment Variables
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/delivery_db
DB_USER=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Google Maps API
GOOGLE_MAPS_API_KEY=your_api_key_here

# JWT
JWT_SECRET=your_jwt_secret_here

# AWS S3
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_S3_BUCKET=delivery-service-files
```

## Monitoring and Observability

### Health Checks
- `/actuator/health` - Service health status
- `/actuator/health/db` - Database connectivity
- `/actuator/health/redis` - Redis connectivity
- `/actuator/metrics` - Application metrics

### Custom Metrics
- Delivery completion rates
- Average delivery times
- Driver utilization rates
- Route optimization efficiency
- Customer satisfaction scores

## Deployment

### Docker Deployment
```bash
# Build and run with Docker
docker build -t delivery-service .
docker run -p 8084:8084 delivery-service
```

### Kubernetes Deployment
```yaml
# Kubernetes deployment configuration available
# in k8s/ directory
kubectl apply -f k8s/
```

## Performance Considerations

- **Database Indexing**: Optimized queries for geospatial operations
- **Caching Strategy**: Redis caching for frequently accessed data
- **Connection Pooling**: Efficient database connection management
- **Async Processing**: Non-blocking operations for better performance
- **Load Balancing**: Horizontal scaling support

## Contributing

1. Follow the established code style and conventions
2. Write comprehensive tests for new features
3. Update documentation for API changes
4. Ensure security best practices are followed
5. Add appropriate logging and monitoring

## License

This project is part of the DoorDash backend microservices architecture.
