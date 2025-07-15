# Restaurant Service

The Restaurant Service is a microservice component of the DoorDash Backend system that manages restaurant information, menu items, and operating hours. It provides APIs for creating, retrieving, updating, and deleting restaurant data, as well as searching for restaurants based on various criteria.

## Features

- Restaurant management (CRUD operations)
- Menu item management (CRUD operations)
- Restaurant hours management
- Restaurant search by various criteria (cuisine, location, etc.)
- Menu item search by various criteria (category, dietary restrictions, etc.)
- Check if a restaurant is open at a specific time
- Caching with Redis for improved performance
- Security with OAuth2 and JWT
- Metrics collection with Micrometer and Prometheus
- Distributed tracing with OpenTelemetry

## Tech Stack

- Java 17
- Spring Boot 3.2.x
- Spring Data JPA
- Spring Security with OAuth2 Resource Server
- PostgreSQL
- Redis for caching
- Flyway for database migrations
- OpenAPI for API documentation
- Resilience4j for circuit breaking and rate limiting
- Micrometer for metrics
- OpenTelemetry for distributed tracing
- Docker for containerization

## API Endpoints

### Restaurant Endpoints

- `GET /api/v1/restaurants` - Get all restaurants (paginated)
- `GET /api/v1/restaurants/{id}` - Get a restaurant by ID
- `POST /api/v1/restaurants/search` - Search restaurants by criteria
- `GET /api/v1/restaurants/{id}/is-open` - Check if a restaurant is open
- `POST /api/v1/restaurants` - Create a new restaurant
- `PUT /api/v1/restaurants/{id}` - Update a restaurant
- `DELETE /api/v1/restaurants/{id}` - Delete a restaurant

### Menu Item Endpoints

- `GET /api/v1/menu-items/{id}` - Get a menu item by ID
- `GET /api/v1/restaurants/{restaurantId}/menu-items` - Get all menu items for a restaurant
- `POST /api/v1/menu-items/search` - Search menu items by criteria
- `POST /api/v1/restaurants/{restaurantId}/menu-items` - Create a new menu item
- `PUT /api/v1/menu-items/{id}` - Update a menu item
- `DELETE /api/v1/menu-items/{id}` - Delete a menu item
- `PATCH /api/v1/menu-items/{id}/toggle-availability` - Toggle menu item availability

## Setup and Running

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Gradle

### Running Locally

1. Clone the repository
2. Navigate to the restaurant-service directory
3. Build the application:
   ```
   ./gradlew build
   ```
4. Run the application:
   ```
   ./gradlew bootRun
   ```

### Running with Docker

1. Build and start the containers:
   ```
   docker-compose up -d
   ```
2. The service will be available at http://localhost:8082

### API Documentation

Once the service is running, you can access the OpenAPI documentation at:

```
http://localhost:8082/swagger-ui.html
```

## Database Schema

The service uses the following database tables:

- `restaurants` - Stores restaurant information
- `restaurant_hours` - Stores restaurant operating hours
- `menu_items` - Stores menu items for restaurants

## Caching

The service uses Redis for caching with the following cache regions:

- `restaurants` - Caches restaurant data
- `menu-items` - Caches menu item data

## Security

The service uses OAuth2 Resource Server with JWT for authentication and authorization. Public endpoints (GET operations) are accessible without authentication, while all other operations require authentication with appropriate scopes.

## Monitoring

The service exposes the following actuator endpoints:

- `/actuator/health` - Health information
- `/actuator/info` - Application information
- `/actuator/prometheus` - Prometheus metrics

## Integration with Other Services

The Restaurant Service integrates with:

- Auth Service - For authentication and authorization
- Order Service - Provides restaurant and menu item data for orders
- Delivery Service - Provides restaurant location data for delivery routing