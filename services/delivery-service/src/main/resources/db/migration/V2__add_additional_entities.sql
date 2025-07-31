-- Migration for additional Delivery Service entities
-- Version: V2__add_additional_entities.sql

-- Create vehicles table
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL CHECK (vehicle_type IN ('BICYCLE', 'MOTORCYCLE', 'SCOOTER', 'CAR', 'VAN', 'TRUCK', 'WALKING')),
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL CHECK (year >= 1900 AND year <= 2030),
    color VARCHAR(30),
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vin VARCHAR(17) UNIQUE,
    
    -- Insurance Information
    insurance_provider VARCHAR(100),
    insurance_policy_number VARCHAR(50),
    insurance_expiry_date DATE,
    
    -- Registration Information
    registration_number VARCHAR(50),
    registration_expiry_date DATE,
    
    -- Inspection Information
    last_inspection_date DATE,
    next_inspection_due DATE,
    inspection_status VARCHAR(20) DEFAULT 'NOT_REQUIRED' CHECK (inspection_status IN ('PASSED', 'FAILED', 'PENDING', 'OVERDUE', 'NOT_REQUIRED')),
    
    -- Vehicle Specifications
    capacity_volume DECIMAL(8,2), -- cubic feet
    capacity_weight DECIMAL(8,2), -- pounds
    fuel_type VARCHAR(20),
    mileage DECIMAL(8,2),
    
    -- Status and Availability
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'INSPECTION_REQUIRED', 'INSURANCE_EXPIRED', 'REGISTRATION_EXPIRED', 'SUSPENDED', 'RETIRED')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verification_date TIMESTAMP,
    verified_by UUID,
    
    -- Maintenance
    last_maintenance_date DATE,
    next_maintenance_due DATE,
    maintenance_notes TEXT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    CONSTRAINT fk_vehicle_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

-- Create delivery_zones table
CREATE TABLE delivery_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    
    -- Geographical boundary (PostGIS Polygon)
    boundary GEOMETRY(POLYGON, 4326) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_codes TEXT, -- Comma-separated list
    
    -- Zone Status and Configuration
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'TEMPORARILY_CLOSED', 'MAINTENANCE', 'CAPACITY_FULL', 'WEATHER_RESTRICTED', 'EMERGENCY_SHUTDOWN')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 1,
    
    -- Delivery Configuration
    max_delivery_distance_km DECIMAL(8,2),
    estimated_delivery_time_minutes INTEGER,
    max_drivers_capacity INTEGER,
    current_active_drivers INTEGER DEFAULT 0,
    
    -- Pricing Configuration
    base_delivery_fee DECIMAL(10,2),
    per_km_rate DECIMAL(10,4),
    surge_multiplier DECIMAL(4,2) DEFAULT 1.00,
    peak_hour_multiplier DECIMAL(4,2) DEFAULT 1.00,
    minimum_order_value DECIMAL(10,2),
    small_order_fee DECIMAL(10,2),
    
    -- Operational Configuration (JSONB)
    operating_hours JSONB,
    peak_hours JSONB,
    delivery_restrictions JSONB,
    special_requirements JSONB,
    
    -- Performance Metrics
    average_delivery_time_minutes DECIMAL(8,2),
    delivery_success_rate DECIMAL(5,4),
    customer_satisfaction_rating DECIMAL(3,2),
    total_deliveries_completed BIGINT DEFAULT 0,
    total_deliveries_failed BIGINT DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Create delivery_tracking table
CREATE TABLE delivery_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    driver_id UUID NOT NULL,
    tracking_timestamp TIMESTAMP NOT NULL,
    
    -- Location Information (PostGIS Point)
    current_location GEOMETRY(POINT, 4326) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    altitude DECIMAL(8,2), -- meters
    accuracy DECIMAL(8,2), -- GPS accuracy in meters
    bearing DECIMAL(6,2), -- Direction in degrees (0-360)
    speed DECIMAL(8,2), -- Speed in km/h
    
    -- Delivery Progress
    tracking_status VARCHAR(30) NOT NULL CHECK (tracking_status IN (
        'DRIVER_ASSIGNED', 'EN_ROUTE_TO_RESTAURANT', 'ARRIVED_AT_RESTAURANT',
        'WAITING_FOR_ORDER', 'ORDER_PICKED_UP', 'EN_ROUTE_TO_CUSTOMER',
        'ARRIVED_AT_DELIVERY_LOCATION', 'DELIVERY_ATTEMPTED', 'DELIVERED',
        'DELIVERY_FAILED', 'RETURNING_TO_RESTAURANT', 'CANCELLED'
    )),
    distance_to_pickup DECIMAL(10,2), -- kilometers
    distance_to_delivery DECIMAL(10,2), -- kilometers
    distance_traveled DECIMAL(10,2), -- Total distance traveled in km
    estimated_pickup_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    route_deviation DECIMAL(8,2), -- Deviation from optimal route in km
    
    -- Milestones and Events
    is_at_restaurant BOOLEAN NOT NULL DEFAULT false,
    restaurant_arrival_time TIMESTAMP,
    pickup_completed_time TIMESTAMP,
    is_en_route_to_customer BOOLEAN NOT NULL DEFAULT false,
    is_at_delivery_location BOOLEAN NOT NULL DEFAULT false,
    delivery_location_arrival_time TIMESTAMP,
    
    -- Geofencing
    restaurant_geofence_entered TIMESTAMP,
    restaurant_geofence_exited TIMESTAMP,
    delivery_geofence_entered TIMESTAMP,
    delivery_geofence_exited TIMESTAMP,
    
    -- Additional Context (JSONB)
    metadata JSONB,
    battery_level INTEGER, -- Driver's device battery level (0-100)
    network_strength INTEGER, -- Signal strength (0-100)
    app_version VARCHAR(20),
    device_id VARCHAR(100),
    
    -- Weather and Traffic
    weather_condition VARCHAR(50),
    temperature DECIMAL(5,2),
    traffic_condition VARCHAR(20) CHECK (traffic_condition IN ('LIGHT', 'MODERATE', 'HEAVY', 'SEVERE', 'UNKNOWN')),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    CONSTRAINT fk_tracking_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_tracking_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

-- Create indexes for performance

-- Vehicles indexes
CREATE INDEX idx_vehicles_driver_id ON vehicles(driver_id);
CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_active ON vehicles(is_active);
CREATE INDEX idx_vehicles_verified ON vehicles(is_verified);
CREATE INDEX idx_vehicles_vehicle_type ON vehicles(vehicle_type);
CREATE INDEX idx_vehicles_insurance_expiry ON vehicles(insurance_expiry_date);

-- Delivery zones indexes
CREATE INDEX idx_delivery_zones_code ON delivery_zones(code);
CREATE INDEX idx_delivery_zones_city ON delivery_zones(city);
CREATE INDEX idx_delivery_zones_state ON delivery_zones(state);
CREATE INDEX idx_delivery_zones_status ON delivery_zones(status);
CREATE INDEX idx_delivery_zones_active ON delivery_zones(is_active);
CREATE INDEX idx_delivery_zones_priority ON delivery_zones(priority);

-- Spatial index for delivery zones boundary
CREATE INDEX idx_delivery_zones_boundary ON delivery_zones USING GIST(boundary);

-- Delivery tracking indexes
CREATE INDEX idx_delivery_tracking_delivery_id ON delivery_tracking(delivery_id);
CREATE INDEX idx_delivery_tracking_driver_id ON delivery_tracking(driver_id);
CREATE INDEX idx_delivery_tracking_timestamp ON delivery_tracking(tracking_timestamp);
CREATE INDEX idx_delivery_tracking_status ON delivery_tracking(tracking_status);

-- Spatial index for delivery tracking location
CREATE INDEX idx_delivery_tracking_location ON delivery_tracking USING GIST(current_location);

-- Update triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_delivery_zones_updated_at BEFORE UPDATE ON delivery_zones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_delivery_tracking_updated_at BEFORE UPDATE ON delivery_tracking
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add foreign key constraints to existing tables if needed
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS delivery_zone_id UUID;
ALTER TABLE drivers ADD CONSTRAINT IF NOT EXISTS fk_driver_delivery_zone 
    FOREIGN KEY (delivery_zone_id) REFERENCES delivery_zones(id);

ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS delivery_zone_id UUID;
ALTER TABLE deliveries ADD CONSTRAINT IF NOT EXISTS fk_delivery_zone 
    FOREIGN KEY (delivery_zone_id) REFERENCES delivery_zones(id);

-- Create index for zone relationships
CREATE INDEX IF NOT EXISTS idx_drivers_delivery_zone_id ON drivers(delivery_zone_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_delivery_zone_id ON deliveries(delivery_zone_id);
