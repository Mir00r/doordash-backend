-- Insert sample restaurants
INSERT INTO restaurants (name, description, address, city, state, zip_code, phone_number, email, website, cuisine, average_rating, total_ratings, delivery_fee, estimated_delivery_time, is_open, image_url)
VALUES
    ('Tasty Burger', 'Delicious burgers and fries', '123 Main St', 'San Francisco', 'CA', '94105', '415-555-1234', 'info@tastyburger.com', 'https://tastyburger.com', 'American', 4.5, 120, 3.99, 30, TRUE, 'https://example.com/images/tastyburger.jpg'),
    ('Pizza Palace', 'Authentic Italian pizza', '456 Market St', 'San Francisco', 'CA', '94103', '415-555-5678', 'info@pizzapalace.com', 'https://pizzapalace.com', 'Italian', 4.2, 85, 2.99, 25, TRUE, 'https://example.com/images/pizzapalace.jpg'),
    ('Sushi Spot', 'Fresh sushi and Japanese cuisine', '789 Mission St', 'San Francisco', 'CA', '94105', '415-555-9012', 'info@sushistop.com', 'https://sushistop.com', 'Japanese', 4.7, 150, 4.99, 35, TRUE, 'https://example.com/images/sushistop.jpg'),
    ('Taco Time', 'Authentic Mexican tacos and burritos', '321 Valencia St', 'San Francisco', 'CA', '94110', '415-555-3456', 'info@tacotime.com', 'https://tacotime.com', 'Mexican', 4.0, 95, 2.49, 20, TRUE, 'https://example.com/images/tacotime.jpg'),
    ('Curry House', 'Flavorful Indian curries and dishes', '555 Howard St', 'San Francisco', 'CA', '94105', '415-555-7890', 'info@curryhouse.com', 'https://curryhouse.com', 'Indian', 4.3, 110, 3.49, 40, TRUE, 'https://example.com/images/curryhouse.jpg'),
    ('Noodle Bar', 'Asian noodles and dumplings', '888 Folsom St', 'San Francisco', 'CA', '94107', '415-555-2345', 'info@noodlebar.com', 'https://noodlebar.com', 'Asian', 4.1, 75, 2.99, 25, TRUE, 'https://example.com/images/noodlebar.jpg'),
    ('Mediterranean Grill', 'Fresh Mediterranean cuisine', '777 Bryant St', 'San Francisco', 'CA', '94107', '415-555-6789', 'info@medgrill.com', 'https://medgrill.com', 'Mediterranean', 4.4, 88, 3.99, 35, TRUE, 'https://example.com/images/medgrill.jpg'),
    ('Vegan Delight', 'Creative plant-based dishes', '444 Brannan St', 'San Francisco', 'CA', '94107', '415-555-0123', 'info@vegandelight.com', 'https://vegandelight.com', 'Vegan', 4.6, 65, 4.49, 30, TRUE, 'https://example.com/images/vegandelight.jpg'),
    ('BBQ Joint', 'Smoky barbecue and comfort food', '222 Townsend St', 'San Francisco', 'CA', '94107', '415-555-4567', 'info@bbqjoint.com', 'https://bbqjoint.com', 'BBQ', 4.8, 130, 5.99, 45, TRUE, 'https://example.com/images/bbqjoint.jpg'),
    ('Breakfast Club', 'All-day breakfast and brunch', '999 Harrison St', 'San Francisco', 'CA', '94107', '415-555-8901', 'info@breakfastclub.com', 'https://breakfastclub.com', 'Breakfast', 4.2, 95, 2.99, 20, TRUE, 'https://example.com/images/breakfastclub.jpg');

-- Insert restaurant hours for Tasty Burger
INSERT INTO restaurant_hours (restaurant_id, day_of_week, open_time, close_time, is_closed)
VALUES
    (1, 'MONDAY', '08:00:00', '22:00:00', FALSE),
    (1, 'TUESDAY', '08:00:00', '22:00:00', FALSE),
    (1, 'WEDNESDAY', '08:00:00', '22:00:00', FALSE),
    (1, 'THURSDAY', '08:00:00', '22:00:00', FALSE),
    (1, 'FRIDAY', '08:00:00', '23:00:00', FALSE),
    (1, 'SATURDAY', '09:00:00', '23:00:00', FALSE),
    (1, 'SUNDAY', '09:00:00', '21:00:00', FALSE);

-- Insert restaurant hours for Pizza Palace
INSERT INTO restaurant_hours (restaurant_id, day_of_week, open_time, close_time, is_closed)
VALUES
    (2, 'MONDAY', '11:00:00', '22:00:00', FALSE),
    (2, 'TUESDAY', '11:00:00', '22:00:00', FALSE),
    (2, 'WEDNESDAY', '11:00:00', '22:00:00', FALSE),
    (2, 'THURSDAY', '11:00:00', '22:00:00', FALSE),
    (2, 'FRIDAY', '11:00:00', '23:30:00', FALSE),
    (2, 'SATURDAY', '11:00:00', '23:30:00', FALSE),
    (2, 'SUNDAY', '12:00:00', '21:00:00', FALSE);

-- Insert restaurant hours for Sushi Spot
INSERT INTO restaurant_hours (restaurant_id, day_of_week, open_time, close_time, is_closed)
VALUES
    (3, 'MONDAY', '11:30:00', '22:00:00', FALSE),
    (3, 'TUESDAY', '11:30:00', '22:00:00', FALSE),
    (3, 'WEDNESDAY', '11:30:00', '22:00:00', FALSE),
    (3, 'THURSDAY', '11:30:00', '22:00:00', FALSE),
    (3, 'FRIDAY', '11:30:00', '23:00:00', FALSE),
    (3, 'SATURDAY', '12:00:00', '23:00:00', FALSE),
    (3, 'SUNDAY', '12:00:00', '21:30:00', FALSE);

-- Insert menu items for Tasty Burger
INSERT INTO menu_items (restaurant_id, name, description, price, category, image_url, available, preparation_time, vegetarian, vegan, gluten_free, spicy)
VALUES
    (1, 'Classic Burger', 'Beef patty with lettuce, tomato, onion, and special sauce', 8.99, 'Burgers', 'https://example.com/images/classic-burger.jpg', TRUE, 15, FALSE, FALSE, FALSE, FALSE),
    (1, 'Cheeseburger', 'Classic burger with American cheese', 9.99, 'Burgers', 'https://example.com/images/cheeseburger.jpg', TRUE, 15, FALSE, FALSE, FALSE, FALSE),
    (1, 'Bacon Burger', 'Classic burger with crispy bacon', 10.99, 'Burgers', 'https://example.com/images/bacon-burger.jpg', TRUE, 18, FALSE, FALSE, FALSE, FALSE),
    (1, 'Veggie Burger', 'Plant-based patty with all the fixings', 9.99, 'Burgers', 'https://example.com/images/veggie-burger.jpg', TRUE, 15, TRUE, TRUE, FALSE, FALSE),
    (1, 'French Fries', 'Crispy golden fries', 3.99, 'Sides', 'https://example.com/images/fries.jpg', TRUE, 10, TRUE, TRUE, FALSE, FALSE),
    (1, 'Onion Rings', 'Crispy battered onion rings', 4.99, 'Sides', 'https://example.com/images/onion-rings.jpg', TRUE, 12, TRUE, FALSE, FALSE, FALSE),
    (1, 'Chocolate Shake', 'Rich and creamy chocolate milkshake', 5.99, 'Drinks', 'https://example.com/images/chocolate-shake.jpg', TRUE, 5, TRUE, FALSE, FALSE, FALSE),
    (1, 'Vanilla Shake', 'Smooth vanilla milkshake', 5.99, 'Drinks', 'https://example.com/images/vanilla-shake.jpg', TRUE, 5, TRUE, FALSE, FALSE, FALSE);

-- Insert menu items for Pizza Palace
INSERT INTO menu_items (restaurant_id, name, description, price, category, image_url, available, preparation_time, vegetarian, vegan, gluten_free, spicy)
VALUES
    (2, 'Margherita Pizza', 'Classic tomato, mozzarella, and basil', 12.99, 'Pizzas', 'https://example.com/images/margherita.jpg', TRUE, 20, TRUE, FALSE, FALSE, FALSE),
    (2, 'Pepperoni Pizza', 'Tomato sauce, mozzarella, and pepperoni', 14.99, 'Pizzas', 'https://example.com/images/pepperoni.jpg', TRUE, 20, FALSE, FALSE, FALSE, FALSE),
    (2, 'Vegetarian Pizza', 'Tomato sauce, mozzarella, bell peppers, onions, mushrooms, and olives', 15.99, 'Pizzas', 'https://example.com/images/vegetarian.jpg', TRUE, 22, TRUE, FALSE, FALSE, FALSE),
    (2, 'Meat Lovers Pizza', 'Tomato sauce, mozzarella, pepperoni, sausage, bacon, and ham', 16.99, 'Pizzas', 'https://example.com/images/meat-lovers.jpg', TRUE, 22, FALSE, FALSE, FALSE, FALSE),
    (2, 'Garlic Bread', 'Toasted bread with garlic butter', 4.99, 'Sides', 'https://example.com/images/garlic-bread.jpg', TRUE, 10, TRUE, FALSE, FALSE, FALSE),
    (2, 'Caesar Salad', 'Romaine lettuce, croutons, parmesan, and Caesar dressing', 7.99, 'Salads', 'https://example.com/images/caesar-salad.jpg', TRUE, 8, TRUE, FALSE, FALSE, FALSE),
    (2, 'Tiramisu', 'Classic Italian coffee-flavored dessert', 6.99, 'Desserts', 'https://example.com/images/tiramisu.jpg', TRUE, 0, TRUE, FALSE, FALSE, FALSE);

-- Insert menu items for Sushi Spot
INSERT INTO menu_items (restaurant_id, name, description, price, category, image_url, available, preparation_time, vegetarian, vegan, gluten_free, spicy)
VALUES
    (3, 'California Roll', 'Crab, avocado, and cucumber', 7.99, 'Rolls', 'https://example.com/images/california-roll.jpg', TRUE, 15, FALSE, FALSE, FALSE, FALSE),
    (3, 'Spicy Tuna Roll', 'Tuna and spicy mayo', 8.99, 'Rolls', 'https://example.com/images/spicy-tuna.jpg', TRUE, 15, FALSE, FALSE, FALSE, TRUE),
    (3, 'Dragon Roll', 'Eel, crab, cucumber, and avocado', 12.99, 'Rolls', 'https://example.com/images/dragon-roll.jpg', TRUE, 18, FALSE, FALSE, FALSE, FALSE),
    (3, 'Vegetable Roll', 'Avocado, cucumber, and carrot', 6.99, 'Rolls', 'https://example.com/images/vegetable-roll.jpg', TRUE, 15, TRUE, TRUE, TRUE, FALSE),
    (3, 'Salmon Nigiri', 'Fresh salmon over rice', 5.99, 'Nigiri', 'https://example.com/images/salmon-nigiri.jpg', TRUE, 10, FALSE, FALSE, TRUE, FALSE),
    (3, 'Tuna Nigiri', 'Fresh tuna over rice', 5.99, 'Nigiri', 'https://example.com/images/tuna-nigiri.jpg', TRUE, 10, FALSE, FALSE, TRUE, FALSE),
    (3, 'Miso Soup', 'Traditional Japanese soup with tofu and seaweed', 3.99, 'Sides', 'https://example.com/images/miso-soup.jpg', TRUE, 5, TRUE, TRUE, TRUE, FALSE),
    (3, 'Edamame', 'Steamed soybeans with salt', 4.99, 'Sides', 'https://example.com/images/edamame.jpg', TRUE, 8, TRUE, TRUE, TRUE, FALSE);