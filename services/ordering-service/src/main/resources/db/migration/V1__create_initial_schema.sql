-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    dasher_id UUID,
    order_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    items JSONB NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    payment_method_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on customer_id for faster lookups
CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- Create index on restaurant_id for faster lookups
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);

-- Create index on status for filtering
CREATE INDEX idx_orders_status ON orders(status);

-- Create cart table
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    items JSONB NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on customer_id for faster lookups
CREATE INDEX idx_carts_customer_id ON carts(customer_id);

-- Create customer_profiles table
CREATE TABLE customer_profiles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create customer_addresses table
CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customer_profiles(id),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on customer_id for faster lookups
CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses(customer_id);

-- Create payment_methods table
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customer_profiles(id),
    payment_provider VARCHAR(50) NOT NULL,
    payment_token VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4) NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    expiry_month INTEGER NOT NULL,
    expiry_year INTEGER NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on customer_id for faster lookups
CREATE INDEX idx_payment_methods_customer_id ON payment_methods(customer_id);