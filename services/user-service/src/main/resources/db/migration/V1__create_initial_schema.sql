-- User Service Database Schema
-- This migration creates the initial schema for user profiles, addresses, and preferences

-- User profiles table
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE, -- Reference to auth-service user
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    profile_picture_url TEXT,
    bio TEXT,
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    verification_level VARCHAR(20) DEFAULT 'BASIC', -- BASIC, PHONE_VERIFIED, ID_VERIFIED, FULL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- User addresses table
CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    label VARCHAR(50) NOT NULL, -- HOME, WORK, OTHER
    street_address VARCHAR(255) NOT NULL,
    apartment_number VARCHAR(50),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'US',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_default BOOLEAN DEFAULT false,
    delivery_instructions TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

-- User preferences table
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    preferred_language VARCHAR(10) DEFAULT 'en',
    preferred_currency VARCHAR(3) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',
    notification_email BOOLEAN DEFAULT true,
    notification_sms BOOLEAN DEFAULT true,
    notification_push BOOLEAN DEFAULT true,
    notification_marketing BOOLEAN DEFAULT false,
    dietary_restrictions JSONB,
    cuisine_preferences JSONB,
    max_delivery_distance INTEGER DEFAULT 10, -- in miles
    default_tip_percentage INTEGER DEFAULT 15,
    auto_reorder_enabled BOOLEAN DEFAULT false,
    dark_mode_enabled BOOLEAN DEFAULT false,
    location_sharing_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

-- User activity logs table
CREATE TABLE user_activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, PROFILE_UPDATE, ADDRESS_ADD, etc.
    activity_description TEXT,
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50), -- WEB, MOBILE_IOS, MOBILE_ANDROID
    location_data JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

-- User sessions table (for tracking active sessions across devices)
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    device_type VARCHAR(50) NOT NULL,
    device_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT true,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

-- User favorites table (restaurants, dishes, etc.)
CREATE TABLE user_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    favorite_type VARCHAR(50) NOT NULL, -- RESTAURANT, DISH, CATEGORY
    favorite_id UUID NOT NULL, -- ID of the favorited item
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE,
    UNIQUE(user_id, favorite_type, favorite_id)
);

-- User payment methods table (stored securely with tokenization)
CREATE TABLE user_payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, etc.
    token VARCHAR(255) NOT NULL, -- Tokenized payment method
    last_four_digits VARCHAR(4),
    card_brand VARCHAR(20), -- VISA, MASTERCARD, AMEX, etc.
    expiry_month INTEGER,
    expiry_year INTEGER,
    cardholder_name VARCHAR(100),
    billing_address_id UUID,
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE,
    FOREIGN KEY (billing_address_id) REFERENCES user_addresses(id) ON DELETE SET NULL
);

-- User delivery preferences table
CREATE TABLE user_delivery_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    preferred_delivery_time VARCHAR(20) DEFAULT 'ASAP', -- ASAP, SCHEDULED
    default_delivery_window INTEGER DEFAULT 30, -- in minutes
    contact_preference VARCHAR(20) DEFAULT 'CALL', -- CALL, TEXT, APP_NOTIFICATION
    special_instructions TEXT,
    leave_at_door BOOLEAN DEFAULT false,
    ring_doorbell BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_active ON user_profiles(is_active);
CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
CREATE INDEX idx_user_addresses_default ON user_addresses(user_id, is_default);
CREATE INDEX idx_user_addresses_location ON user_addresses(latitude, longitude);
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX idx_user_activity_logs_user_id ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_type ON user_activity_logs(activity_type);
CREATE INDEX idx_user_activity_logs_created_at ON user_activity_logs(created_at);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_active ON user_sessions(is_active);
CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_type ON user_favorites(favorite_type);
CREATE INDEX idx_user_payment_methods_user_id ON user_payment_methods(user_id);
CREATE INDEX idx_user_payment_methods_active ON user_payment_methods(is_active);
CREATE INDEX idx_user_delivery_preferences_user_id ON user_delivery_preferences(user_id);

-- Add comments for documentation
COMMENT ON TABLE user_profiles IS 'User profile information and basic details';
COMMENT ON TABLE user_addresses IS 'User addresses for delivery and billing';
COMMENT ON TABLE user_preferences IS 'User preferences and settings';
COMMENT ON TABLE user_activity_logs IS 'Log of user activities for analytics and security';
COMMENT ON TABLE user_sessions IS 'Active user sessions across devices';
COMMENT ON TABLE user_favorites IS 'User favorite restaurants, dishes, and categories';
COMMENT ON TABLE user_payment_methods IS 'Tokenized user payment methods';
COMMENT ON TABLE user_delivery_preferences IS 'User delivery preferences and instructions';
