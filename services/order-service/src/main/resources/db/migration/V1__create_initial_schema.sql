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