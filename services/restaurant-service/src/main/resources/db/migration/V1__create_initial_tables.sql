-- Create restaurants table
CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(255),
    cuisine VARCHAR(100) NOT NULL,
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.0,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10,2) NOT NULL,
    estimated_delivery_time INTEGER NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create restaurant_hours table
CREATE TABLE restaurant_hours (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_restaurant_hours_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- Create menu_items table
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    preparation_time INTEGER,
    vegetarian BOOLEAN DEFAULT FALSE,
    vegan BOOLEAN DEFAULT FALSE,
    gluten_free BOOLEAN DEFAULT FALSE,
    spicy BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_restaurants_cuisine ON restaurants(cuisine);
CREATE INDEX idx_restaurants_city ON restaurants(city);
CREATE INDEX idx_restaurants_zip_code ON restaurants(zip_code);
CREATE INDEX idx_restaurants_is_open ON restaurants(is_open);
CREATE INDEX idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_menu_items_available ON menu_items(available);
CREATE INDEX idx_restaurant_hours_restaurant_id ON restaurant_hours(restaurant_id);
CREATE INDEX idx_restaurant_hours_day_of_week ON restaurant_hours(day_of_week);