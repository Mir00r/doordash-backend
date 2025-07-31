-- Delivery Service Database Schema
-- This migration creates the initial schema for delivery operations, driver management, and tracking

-- Enable PostGIS extension for geospatial operations
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Drivers table - Driver registration and profile information
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE, -- Reference to auth-service user
    driver_license_number VARCHAR(50) NOT NULL UNIQUE,
    license_expiry_date DATE NOT NULL,
    background_check_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, EXPIRED
    background_check_date TIMESTAMP,
    phone_number VARCHAR(20) NOT NULL,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    bank_account_number VARCHAR(50), -- Encrypted
    routing_number VARCHAR(20), -- Encrypted
    tax_id VARCHAR(20), -- Encrypted
    driver_status VARCHAR(20) DEFAULT 'INACTIVE', -- INACTIVE, ACTIVE, SUSPENDED, BANNED
    availability_status VARCHAR(20) DEFAULT 'OFFLINE', -- OFFLINE, AVAILABLE, BUSY, ON_BREAK
    current_location GEOMETRY(Point, 4326),
    last_location_update TIMESTAMP,
    total_deliveries INTEGER DEFAULT 0,
    successful_deliveries INTEGER DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Vehicles table - Vehicle registration and information
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL, -- CAR, MOTORCYCLE, BICYCLE, SCOOTER, WALKING
    make VARCHAR(50),
    model VARCHAR(50),
    year INTEGER,
    color VARCHAR(30),
    license_plate VARCHAR(20),
    insurance_policy_number VARCHAR(50),
    insurance_expiry_date DATE,
    registration_number VARCHAR(50),
    registration_expiry_date DATE,
    is_primary BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Delivery zones table - Geographic service areas
CREATE TABLE delivery_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    boundary GEOMETRY(Polygon, 4326) NOT NULL,
    center_point GEOMETRY(Point, 4326) NOT NULL,
    base_delivery_fee DECIMAL(5, 2) DEFAULT 2.99,
    per_mile_fee DECIMAL(5, 2) DEFAULT 0.99,
    surge_multiplier DECIMAL(3, 2) DEFAULT 1.00,
    is_active BOOLEAN DEFAULT true,
    peak_hours_start TIME,
    peak_hours_end TIME,
    max_delivery_radius_miles INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Deliveries table - Core delivery requests and tracking
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE, -- Reference to order-service order
    customer_id UUID NOT NULL, -- Reference to user-service customer
    restaurant_id UUID NOT NULL, -- Reference to restaurant-service restaurant
    driver_id UUID, -- Assigned driver (nullable until assigned)
    delivery_zone_id UUID,
    
    -- Addresses
    pickup_address_line1 VARCHAR(255) NOT NULL,
    pickup_address_line2 VARCHAR(255),
    pickup_city VARCHAR(100) NOT NULL,
    pickup_state VARCHAR(100) NOT NULL,
    pickup_postal_code VARCHAR(20) NOT NULL,
    pickup_location GEOMETRY(Point, 4326),
    pickup_instructions TEXT,
    
    delivery_address_line1 VARCHAR(255) NOT NULL,
    delivery_address_line2 VARCHAR(255),
    delivery_city VARCHAR(100) NOT NULL,
    delivery_state VARCHAR(100) NOT NULL,
    delivery_postal_code VARCHAR(20) NOT NULL,
    delivery_location GEOMETRY(Point, 4326),
    delivery_instructions TEXT,
    
    -- Status and timing
    status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED, FAILED
    priority VARCHAR(20) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    estimated_pickup_time TIMESTAMP,
    actual_pickup_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    
    -- Pricing
    base_fee DECIMAL(5, 2) DEFAULT 2.99,
    delivery_fee DECIMAL(5, 2),
    surge_multiplier DECIMAL(3, 2) DEFAULT 1.00,
    tip_amount DECIMAL(5, 2) DEFAULT 0.00,
    total_amount DECIMAL(8, 2),
    driver_payout DECIMAL(8, 2),
    
    -- Distance and route
    total_distance_miles DECIMAL(6, 2),
    pickup_distance_miles DECIMAL(6, 2),
    delivery_distance_miles DECIMAL(6, 2),
    estimated_duration_minutes INTEGER,
    actual_duration_minutes INTEGER,
    
    -- Metadata
    special_requirements JSONB,
    delivery_notes TEXT,
    customer_rating INTEGER CHECK (customer_rating >= 1 AND customer_rating <= 5),
    customer_feedback TEXT,
    driver_notes TEXT,
    
    -- Tracking
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL,
    FOREIGN KEY (delivery_zone_id) REFERENCES delivery_zones(id) ON DELETE SET NULL
);

-- Delivery tracking table - Real-time location and status updates
CREATE TABLE delivery_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    driver_id UUID NOT NULL,
    current_location GEOMETRY(Point, 4326) NOT NULL,
    heading DECIMAL(5, 2), -- Direction in degrees (0-360)
    speed_mph DECIMAL(5, 2),
    accuracy_meters INTEGER,
    battery_level INTEGER,
    status VARCHAR(30) NOT NULL,
    eta_minutes INTEGER,
    distance_remaining_miles DECIMAL(6, 2),
    is_delayed BOOLEAN DEFAULT false,
    delay_reason VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Routes table - Optimized delivery routes for multiple deliveries
CREATE TABLE routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    route_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PLANNED', -- PLANNED, ACTIVE, COMPLETED, CANCELLED
    total_deliveries INTEGER DEFAULT 0,
    completed_deliveries INTEGER DEFAULT 0,
    total_distance_miles DECIMAL(8, 2),
    estimated_duration_minutes INTEGER,
    actual_duration_minutes INTEGER,
    route_geometry GEOMETRY(LineString, 4326),
    optimization_score DECIMAL(5, 2), -- Efficiency score
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Route stops table - Individual stops in a route
CREATE TABLE route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id UUID NOT NULL,
    delivery_id UUID NOT NULL,
    stop_order INTEGER NOT NULL,
    stop_type VARCHAR(20) NOT NULL, -- PICKUP, DELIVERY
    address VARCHAR(500) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    estimated_arrival_time TIMESTAMP,
    actual_arrival_time TIMESTAMP,
    estimated_duration_minutes INTEGER DEFAULT 5,
    actual_duration_minutes INTEGER,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ARRIVED, COMPLETED, SKIPPED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE,
    UNIQUE(route_id, stop_order)
);

-- Driver earnings table - Earnings tracking and payout information
CREATE TABLE driver_earnings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    delivery_id UUID,
    earning_type VARCHAR(30) NOT NULL, -- DELIVERY, TIP, BONUS, INCENTIVE, ADJUSTMENT
    base_amount DECIMAL(8, 2) DEFAULT 0.00,
    tip_amount DECIMAL(8, 2) DEFAULT 0.00,
    bonus_amount DECIMAL(8, 2) DEFAULT 0.00,
    total_amount DECIMAL(8, 2) NOT NULL,
    payout_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, FAILED, CANCELLED
    payout_date TIMESTAMP,
    payout_reference VARCHAR(100),
    tax_withheld DECIMAL(8, 2) DEFAULT 0.00,
    fees_deducted DECIMAL(8, 2) DEFAULT 0.00,
    net_amount DECIMAL(8, 2) NOT NULL,
    earning_date DATE NOT NULL,
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE SET NULL
);

-- Driver ratings table - Customer ratings for drivers
CREATE TABLE driver_ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL UNIQUE,
    driver_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT,
    rating_categories JSONB, -- {"punctuality": 5, "communication": 4, "professionalism": 5}
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Driver documents table - Document management for verification
CREATE TABLE driver_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL, -- LICENSE, INSURANCE, REGISTRATION, BACKGROUND_CHECK, VEHICLE_INSPECTION
    document_url VARCHAR(500) NOT NULL,
    document_name VARCHAR(255),
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    verification_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED, EXPIRED
    verification_date TIMESTAMP,
    verified_by VARCHAR(100),
    expiry_date DATE,
    rejection_reason TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Delivery events table - Event sourcing for delivery lifecycle
CREATE TABLE delivery_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- CREATED, ASSIGNED, DRIVER_ARRIVING, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED, FAILED
    event_data JSONB,
    actor_id UUID, -- Who triggered the event (driver, customer, system)
    actor_type VARCHAR(20), -- DRIVER, CUSTOMER, SYSTEM, ADMIN
    location GEOMETRY(Point, 4326),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Driver availability table - Driver schedule and availability tracking
CREATE TABLE driver_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6), -- 0=Sunday, 6=Saturday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    delivery_zone_id UUID,
    is_available BOOLEAN DEFAULT true,
    max_deliveries INTEGER DEFAULT 10,
    break_start_time TIME,
    break_end_time TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_zone_id) REFERENCES delivery_zones(id) ON DELETE SET NULL,
    UNIQUE(driver_id, day_of_week, start_time)
);

-- Delivery analytics table - Aggregated analytics and metrics
CREATE TABLE delivery_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date DATE NOT NULL,
    hour_of_day INTEGER CHECK (hour_of_day >= 0 AND hour_of_day <= 23),
    delivery_zone_id UUID,
    total_deliveries INTEGER DEFAULT 0,
    successful_deliveries INTEGER DEFAULT 0,
    cancelled_deliveries INTEGER DEFAULT 0,
    average_delivery_time_minutes DECIMAL(6, 2),
    average_rating DECIMAL(3, 2),
    total_distance_miles DECIMAL(10, 2),
    total_earnings DECIMAL(12, 2),
    peak_demand_multiplier DECIMAL(3, 2),
    active_drivers INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (delivery_zone_id) REFERENCES delivery_zones(id) ON DELETE SET NULL,
    UNIQUE(date, hour_of_day, delivery_zone_id)
);

-- Indexes for performance optimization
CREATE INDEX idx_drivers_user_id ON drivers(user_id);
CREATE INDEX idx_drivers_status ON drivers(driver_status, availability_status);
CREATE INDEX idx_drivers_location ON drivers USING GIST(current_location);
CREATE INDEX idx_drivers_rating ON drivers(average_rating DESC);

CREATE INDEX idx_vehicles_driver_id ON vehicles(driver_id);
CREATE INDEX idx_vehicles_primary ON vehicles(driver_id, is_primary);

CREATE INDEX idx_delivery_zones_active ON delivery_zones(is_active);
CREATE INDEX idx_delivery_zones_boundary ON delivery_zones USING GIST(boundary);
CREATE INDEX idx_delivery_zones_center ON delivery_zones USING GIST(center_point);

CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_customer_id ON deliveries(customer_id);
CREATE INDEX idx_deliveries_restaurant_id ON deliveries(restaurant_id);
CREATE INDEX idx_deliveries_driver_id ON deliveries(driver_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at);
CREATE INDEX idx_deliveries_pickup_location ON deliveries USING GIST(pickup_location);
CREATE INDEX idx_deliveries_delivery_location ON deliveries USING GIST(delivery_location);
CREATE INDEX idx_deliveries_estimated_pickup ON deliveries(estimated_pickup_time);
CREATE INDEX idx_deliveries_estimated_delivery ON deliveries(estimated_delivery_time);

CREATE INDEX idx_delivery_tracking_delivery_id ON delivery_tracking(delivery_id);
CREATE INDEX idx_delivery_tracking_driver_id ON delivery_tracking(driver_id);
CREATE INDEX idx_delivery_tracking_timestamp ON delivery_tracking(timestamp DESC);
CREATE INDEX idx_delivery_tracking_location ON delivery_tracking USING GIST(current_location);

CREATE INDEX idx_routes_driver_id ON routes(driver_id);
CREATE INDEX idx_routes_status ON routes(status);
CREATE INDEX idx_routes_created_at ON routes(created_at);

CREATE INDEX idx_route_stops_route_id ON route_stops(route_id);
CREATE INDEX idx_route_stops_delivery_id ON route_stops(delivery_id);
CREATE INDEX idx_route_stops_order ON route_stops(route_id, stop_order);

CREATE INDEX idx_driver_earnings_driver_id ON driver_earnings(driver_id);
CREATE INDEX idx_driver_earnings_date ON driver_earnings(earning_date);
CREATE INDEX idx_driver_earnings_payout_status ON driver_earnings(payout_status);

CREATE INDEX idx_driver_ratings_driver_id ON driver_ratings(driver_id);
CREATE INDEX idx_driver_ratings_customer_id ON driver_ratings(customer_id);
CREATE INDEX idx_driver_ratings_rating ON driver_ratings(rating);

CREATE INDEX idx_driver_documents_driver_id ON driver_documents(driver_id);
CREATE INDEX idx_driver_documents_type ON driver_documents(document_type);
CREATE INDEX idx_driver_documents_status ON driver_documents(verification_status);

CREATE INDEX idx_delivery_events_delivery_id ON delivery_events(delivery_id);
CREATE INDEX idx_delivery_events_type ON delivery_events(event_type);
CREATE INDEX idx_delivery_events_timestamp ON delivery_events(timestamp DESC);

CREATE INDEX idx_driver_availability_driver_id ON driver_availability(driver_id);
CREATE INDEX idx_driver_availability_day ON driver_availability(day_of_week);

CREATE INDEX idx_delivery_analytics_date ON delivery_analytics(date);
CREATE INDEX idx_delivery_analytics_zone ON delivery_analytics(delivery_zone_id);

-- Add table comments for documentation
COMMENT ON TABLE drivers IS 'Driver registration and profile information';
COMMENT ON TABLE vehicles IS 'Vehicle registration and information for drivers';
COMMENT ON TABLE delivery_zones IS 'Geographic service areas for deliveries';
COMMENT ON TABLE deliveries IS 'Core delivery requests and tracking information';
COMMENT ON TABLE delivery_tracking IS 'Real-time location and status updates during delivery';
COMMENT ON TABLE routes IS 'Optimized delivery routes for multiple deliveries';
COMMENT ON TABLE route_stops IS 'Individual stops within a delivery route';
COMMENT ON TABLE driver_earnings IS 'Driver earnings tracking and payout information';
COMMENT ON TABLE driver_ratings IS 'Customer ratings and feedback for drivers';
COMMENT ON TABLE driver_documents IS 'Document management for driver verification';
COMMENT ON TABLE delivery_events IS 'Event sourcing for delivery lifecycle tracking';
COMMENT ON TABLE driver_availability IS 'Driver schedule and availability management';
COMMENT ON TABLE delivery_analytics IS 'Aggregated analytics and performance metrics';
