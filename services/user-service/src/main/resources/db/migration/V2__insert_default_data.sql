-- Insert default data for User Service

-- Insert default dietary restrictions options (for reference)
-- These will be used in the application for dropdown options
INSERT INTO user_preferences (user_id, dietary_restrictions, created_at) VALUES 
('00000000-0000-0000-0000-000000000000', '["Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Nut-Free", "Keto", "Paleo", "Halal", "Kosher", "Low-Sodium"]', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Insert default cuisine preferences options (for reference)
INSERT INTO user_preferences (user_id, cuisine_preferences, created_at) VALUES 
('00000000-0000-0000-0000-000000000001', '["American", "Italian", "Chinese", "Mexican", "Indian", "Thai", "Japanese", "Mediterranean", "French", "Korean", "Vietnamese", "Greek", "Middle Eastern", "Pizza", "Burgers", "Sushi", "Desserts", "Healthy", "Fast Food"]', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Create default system user profiles for integration testing
INSERT INTO user_profiles (
    id,
    user_id,
    first_name,
    last_name,
    display_name,
    phone_number,
    is_active,
    is_verified,
    verification_level,
    created_by
) VALUES 
(
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    'Test',
    'User',
    'Test User',
    '+1234567890',
    true,
    true,
    'FULL',
    'SYSTEM'
),
(
    '22222222-2222-2222-2222-222222222222',
    '22222222-2222-2222-2222-222222222222',
    'Admin',
    'User',
    'Admin User',
    '+1234567891',
    true,
    true,
    'FULL',
    'SYSTEM'
)
ON CONFLICT (user_id) DO NOTHING;

-- Create default addresses for test users
INSERT INTO user_addresses (
    id,
    user_id,
    label,
    street_address,
    city,
    state,
    postal_code,
    country,
    latitude,
    longitude,
    is_default,
    delivery_instructions,
    created_by
) VALUES 
(
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'HOME',
    '123 Main Street',
    'San Francisco',
    'CA',
    '94105',
    'US',
    37.7749,
    -122.4194,
    true,
    'Ring doorbell twice',
    'SYSTEM'
),
(
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111',
    'WORK',
    '456 Tech Street',
    'San Francisco',
    'CA',
    '94107',
    'US',
    37.7849,
    -122.4094,
    false,
    'Leave with reception',
    'SYSTEM'
)
ON CONFLICT (id) DO NOTHING;

-- Create default preferences for test users
INSERT INTO user_preferences (
    id,
    user_id,
    preferred_language,
    preferred_currency,
    timezone,
    notification_email,
    notification_sms,
    notification_push,
    notification_marketing,
    dietary_restrictions,
    cuisine_preferences,
    max_delivery_distance,
    default_tip_percentage,
    created_by
) VALUES 
(
    '55555555-5555-5555-5555-555555555555',
    '11111111-1111-1111-1111-111111111111',
    'en',
    'USD',
    'America/Los_Angeles',
    true,
    true,
    true,
    false,
    '["Vegetarian"]',
    '["Italian", "Mexican", "Sushi"]',
    15,
    18,
    'SYSTEM'
)
ON CONFLICT (user_id) DO NOTHING;

-- Create default delivery preferences for test users
INSERT INTO user_delivery_preferences (
    id,
    user_id,
    preferred_delivery_time,
    default_delivery_window,
    contact_preference,
    special_instructions,
    leave_at_door,
    ring_doorbell
) VALUES 
(
    '66666666-6666-6666-6666-666666666666',
    '11111111-1111-1111-1111-111111111111',
    'ASAP',
    30,
    'APP_NOTIFICATION',
    'Please call when you arrive',
    false,
    true
)
ON CONFLICT (user_id) DO NOTHING;

-- Remove the placeholder entries
DELETE FROM user_preferences WHERE user_id IN ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000001');
