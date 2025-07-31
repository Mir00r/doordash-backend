-- Sample data for Delivery Service
-- Version: V3__insert_sample_data.sql

-- Insert sample delivery zones
INSERT INTO delivery_zones (id, name, code, description, boundary, city, state, country, postal_codes, status, is_active, priority, max_delivery_distance_km, estimated_delivery_time_minutes, max_drivers_capacity, base_delivery_fee, per_km_rate, surge_multiplier, minimum_order_value, small_order_fee, operating_hours, peak_hours) VALUES
(
    '550e8400-e29b-41d4-a716-446655440001',
    'Downtown San Francisco',
    'SF_DT_001',
    'Main downtown area covering Financial District and SOMA',
    ST_GeomFromText('POLYGON((-122.4194 37.7749, -122.3959 37.7749, -122.3959 37.8049, -122.4194 37.8049, -122.4194 37.7749))', 4326),
    'San Francisco',
    'California',
    'USA',
    '94105,94107,94108,94111',
    'ACTIVE',
    true,
    1,
    15.00,
    35,
    50,
    4.99,
    1.25,
    1.00,
    20.00,
    2.99,
    '{"monday": "06:00-23:00", "tuesday": "06:00-23:00", "wednesday": "06:00-23:00", "thursday": "06:00-23:00", "friday": "06:00-01:00", "saturday": "07:00-01:00", "sunday": "07:00-23:00"}',
    '{"monday": "11:30-13:30,18:00-20:00", "tuesday": "11:30-13:30,18:00-20:00", "wednesday": "11:30-13:30,18:00-20:00", "thursday": "11:30-13:30,18:00-20:00", "friday": "11:30-13:30,18:00-21:00", "saturday": "12:00-14:00,18:00-21:00", "sunday": "12:00-14:00,18:00-20:00"}'
),
(
    '550e8400-e29b-41d4-a716-446655440002',
    'Mission District',
    'SF_MS_001',
    'Mission District and surrounding neighborhoods',
    ST_GeomFromText('POLYGON((-122.4300 37.7400, -122.4000 37.7400, -122.4000 37.7700, -122.4300 37.7700, -122.4300 37.7400))', 4326),
    'San Francisco',
    'California',
    'USA',
    '94110,94112,94114',
    'ACTIVE',
    true,
    2,
    12.00,
    30,
    40,
    3.99,
    1.20,
    1.10,
    18.00,
    2.49,
    '{"monday": "07:00-22:00", "tuesday": "07:00-22:00", "wednesday": "07:00-22:00", "thursday": "07:00-22:00", "friday": "07:00-23:00", "saturday": "08:00-23:00", "sunday": "08:00-22:00"}',
    '{"monday": "12:00-14:00,19:00-21:00", "tuesday": "12:00-14:00,19:00-21:00", "wednesday": "12:00-14:00,19:00-21:00", "thursday": "12:00-14:00,19:00-21:00", "friday": "12:00-14:00,19:00-22:00", "saturday": "12:00-15:00,19:00-22:00", "sunday": "12:00-15:00,19:00-21:00"}'
),
(
    '550e8400-e29b-41d4-a716-446655440003',
    'Berkeley',
    'BK_001',
    'Berkeley and North Oakland area',
    ST_GeomFromText('POLYGON((-122.3200 37.8500, -122.2400 37.8500, -122.2400 37.9000, -122.3200 37.9000, -122.3200 37.8500))', 4326),
    'Berkeley',
    'California',
    'USA',
    '94702,94703,94704,94705,94707,94708,94709,94710',
    'ACTIVE',
    true,
    3,
    18.00,
    40,
    35,
    4.49,
    1.15,
    1.00,
    22.00,
    2.99,
    '{"monday": "07:00-22:00", "tuesday": "07:00-22:00", "wednesday": "07:00-22:00", "thursday": "07:00-22:00", "friday": "07:00-23:00", "saturday": "08:00-23:00", "sunday": "08:00-22:00"}',
    '{"monday": "12:00-14:00,18:30-20:30", "tuesday": "12:00-14:00,18:30-20:30", "wednesday": "12:00-14:00,18:30-20:30", "thursday": "12:00-14:00,18:30-20:30", "friday": "12:00-14:00,18:30-21:30", "saturday": "12:00-15:00,18:30-21:30", "sunday": "12:00-15:00,18:30-20:30"}'
);

-- Update existing drivers with delivery zones
UPDATE drivers SET delivery_zone_id = '550e8400-e29b-41d4-a716-446655440001' WHERE email = 'john.driver@example.com';
UPDATE drivers SET delivery_zone_id = '550e8400-e29b-41d4-a716-446655440002' WHERE email = 'jane.smith@example.com';
UPDATE drivers SET delivery_zone_id = '550e8400-e29b-41d4-a716-446655440003' WHERE email = 'mike.wilson@example.com';

-- Insert sample vehicles for drivers
INSERT INTO vehicles (id, driver_id, vehicle_type, make, model, year, color, license_plate, vin, insurance_provider, insurance_policy_number, insurance_expiry_date, registration_number, registration_expiry_date, inspection_status, capacity_volume, capacity_weight, fuel_type, mileage, status, is_active, is_verified, verification_date) VALUES
(
    '660e8400-e29b-41d4-a716-446655440001',
    (SELECT id FROM drivers WHERE email = 'john.driver@example.com' LIMIT 1),
    'CAR',
    'Toyota',
    'Camry',
    2022,
    'Silver',
    '8ABC123',
    '1HGCM82633A123456',
    'State Farm',
    'SF-123456789',
    '2025-06-15',
    'REG-789456',
    '2025-03-20',
    'PASSED',
    15.5,
    1000.0,
    'Gasoline',
    32.5,
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '30 days'
),
(
    '660e8400-e29b-41d4-a716-446655440002',
    (SELECT id FROM drivers WHERE email = 'jane.smith@example.com' LIMIT 1),
    'MOTORCYCLE',
    'Honda',
    'CBR600RR',
    2021,
    'Red',
    '2XYZ789',
    'JH2PC4000LM000001',
    'Geico',
    'GC-987654321',
    '2025-08-10',
    'REG-456789',
    '2025-05-15',
    'PASSED',
    2.0,
    50.0,
    'Gasoline',
    55.0,
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '45 days'
),
(
    '660e8400-e29b-41d4-a716-446655440003',
    (SELECT id FROM drivers WHERE email = 'mike.wilson@example.com' LIMIT 1),
    'BICYCLE',
    'Trek',
    'FX 3 Disc',
    2023,
    'Blue',
    'BK001',
    null,
    null,
    null,
    null,
    null,
    null,
    'NOT_REQUIRED',
    1.0,
    25.0,
    'Human',
    0.0,
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '15 days'
);

-- Insert sample delivery tracking data for existing deliveries
INSERT INTO delivery_tracking (id, delivery_id, driver_id, tracking_timestamp, current_location, latitude, longitude, accuracy, bearing, speed, tracking_status, distance_to_pickup, distance_to_delivery, distance_traveled, estimated_pickup_time, estimated_delivery_time, is_at_restaurant, is_en_route_to_customer, is_at_delivery_location, battery_level, network_strength, app_version, traffic_condition) VALUES
(
    '770e8400-e29b-41d4-a716-446655440001',
    (SELECT id FROM deliveries WHERE status = 'EN_ROUTE' LIMIT 1),
    (SELECT id FROM drivers WHERE email = 'john.driver@example.com' LIMIT 1),
    CURRENT_TIMESTAMP - INTERVAL '5 minutes',
    ST_Point(-122.4194, 37.7849),
    37.7849,
    -122.4194,
    10.0,
    45.0,
    25.5,
    'EN_ROUTE_TO_CUSTOMER',
    0.0,
    2.5,
    8.2,
    CURRENT_TIMESTAMP - INTERVAL '10 minutes',
    CURRENT_TIMESTAMP + INTERVAL '15 minutes',
    false,
    true,
    false,
    85,
    90,
    '2.1.0',
    'MODERATE'
),
(
    '770e8400-e29b-41d4-a716-446655440002',
    (SELECT id FROM deliveries WHERE status = 'PICKUP_IN_PROGRESS' LIMIT 1),
    (SELECT id FROM drivers WHERE email = 'jane.smith@example.com' LIMIT 1),
    CURRENT_TIMESTAMP - INTERVAL '2 minutes',
    ST_Point(-122.4100, 37.7600),
    37.7600,
    -122.4100,
    8.0,
    180.0,
    0.0,
    'ARRIVED_AT_RESTAURANT',
    0.1,
    5.8,
    3.2,
    CURRENT_TIMESTAMP + INTERVAL '5 minutes',
    CURRENT_TIMESTAMP + INTERVAL '25 minutes',
    true,
    false,
    false,
    92,
    85,
    '2.1.0',
    'LIGHT'
);

-- Update delivery zone statistics
UPDATE delivery_zones SET 
    current_active_drivers = (
        SELECT COUNT(*) 
        FROM drivers 
        WHERE delivery_zone_id = delivery_zones.id 
        AND is_active = true 
        AND availability_status = 'AVAILABLE'
    ),
    total_deliveries_completed = FLOOR(RANDOM() * 1000) + 500,
    total_deliveries_failed = FLOOR(RANDOM() * 50) + 10,
    average_delivery_time_minutes = 25.0 + (RANDOM() * 20),
    delivery_success_rate = 0.92 + (RANDOM() * 0.06),
    customer_satisfaction_rating = 4.2 + (RANDOM() * 0.7);

-- Insert some operational configuration examples
UPDATE delivery_zones SET 
    delivery_restrictions = '{"no_alcohol_before": "09:00", "no_alcohol_after": "22:00", "max_weight_kg": 15, "restricted_items": ["tobacco", "firearms"]}',
    special_requirements = '{"contactless_delivery": true, "photo_verification": true, "signature_required_above": 50}'
WHERE code = 'SF_DT_001';

UPDATE delivery_zones SET 
    delivery_restrictions = '{"max_stairs": 3, "elevator_required_above_floor": 3, "parking_restrictions": ["street_cleaning_tuesday"]}',
    special_requirements = '{"building_access_code": true, "safe_drop_allowed": false}'
WHERE code = 'SF_MS_001';

-- Commit the transaction
COMMIT;
