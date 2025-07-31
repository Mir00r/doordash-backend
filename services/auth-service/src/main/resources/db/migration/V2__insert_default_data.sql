-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'System administrator with full access'),
('CUSTOMER', 'Regular customer user'),
('RESTAURANT_OWNER', 'Restaurant owner or manager'),
('DELIVERY_DRIVER', 'Delivery driver'),
('SUPPORT_AGENT', 'Customer support agent'),
('RESTAURANT_STAFF', 'Restaurant staff member');

-- Insert default permissions
INSERT INTO permissions (name, resource, action, description) VALUES
-- User management permissions
('user.read', 'user', 'read', 'Read user information'),
('user.write', 'user', 'write', 'Create and update user information'),
('user.delete', 'user', 'delete', 'Delete user accounts'),
('user.admin', 'user', 'admin', 'Full user administration'),

-- Restaurant permissions
('restaurant.read', 'restaurant', 'read', 'Read restaurant information'),
('restaurant.write', 'restaurant', 'write', 'Create and update restaurant information'),
('restaurant.delete', 'restaurant', 'delete', 'Delete restaurants'),
('restaurant.manage', 'restaurant', 'manage', 'Manage own restaurant'),

-- Menu permissions
('menu.read', 'menu', 'read', 'Read menu items'),
('menu.write', 'menu', 'write', 'Create and update menu items'),
('menu.delete', 'menu', 'delete', 'Delete menu items'),
('menu.manage', 'menu', 'manage', 'Manage own restaurant menu'),

-- Order permissions
('order.read', 'order', 'read', 'Read order information'),
('order.write', 'order', 'write', 'Create and update orders'),
('order.cancel', 'order', 'cancel', 'Cancel orders'),
('order.manage', 'order', 'manage', 'Manage restaurant orders'),
('order.admin', 'order', 'admin', 'Full order administration'),

-- Delivery permissions
('delivery.read', 'delivery', 'read', 'Read delivery information'),
('delivery.write', 'delivery', 'write', 'Update delivery status'),
('delivery.assign', 'delivery', 'assign', 'Assign deliveries to drivers'),
('delivery.manage', 'delivery', 'manage', 'Manage own deliveries'),

-- Payment permissions
('payment.read', 'payment', 'read', 'Read payment information'),
('payment.process', 'payment', 'process', 'Process payments'),
('payment.refund', 'payment', 'refund', 'Process refunds'),
('payment.admin', 'payment', 'admin', 'Full payment administration'),

-- System permissions
('system.admin', 'system', 'admin', 'System administration'),
('system.monitor', 'system', 'monitor', 'System monitoring'),
('analytics.read', 'analytics', 'read', 'Read analytics data'),
('support.read', 'support', 'read', 'Read support tickets'),
('support.write', 'support', 'write', 'Create and update support tickets');

-- Assign permissions to roles
-- ADMIN role - full access
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ADMIN';

-- CUSTOMER role - basic customer permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'CUSTOMER' 
AND p.name IN (
    'user.read', 'restaurant.read', 'menu.read', 'order.read', 'order.write', 
    'order.cancel', 'delivery.read', 'payment.read', 'support.write'
);

-- RESTAURANT_OWNER role - restaurant management permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'RESTAURANT_OWNER' 
AND p.name IN (
    'user.read', 'restaurant.read', 'restaurant.manage', 'menu.read', 
    'menu.write', 'menu.delete', 'menu.manage', 'order.read', 'order.manage', 
    'delivery.read', 'payment.read', 'analytics.read', 'support.write'
);

-- DELIVERY_DRIVER role - delivery permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'DELIVERY_DRIVER' 
AND p.name IN (
    'user.read', 'delivery.read', 'delivery.write', 'delivery.manage', 
    'order.read', 'support.write'
);

-- SUPPORT_AGENT role - customer support permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'SUPPORT_AGENT' 
AND p.name IN (
    'user.read', 'user.write', 'restaurant.read', 'menu.read', 'order.read', 
    'order.write', 'delivery.read', 'payment.read', 'support.read', 
    'support.write', 'analytics.read'
);

-- RESTAURANT_STAFF role - basic restaurant staff permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'RESTAURANT_STAFF' 
AND p.name IN (
    'user.read', 'restaurant.read', 'menu.read', 'order.read', 'order.manage', 
    'delivery.read', 'support.write'
);

-- Insert default admin user (password: Admin123!)
-- Note: This should be changed immediately after deployment
INSERT INTO users (
    email, username, password_hash, first_name, last_name, 
    is_email_verified, is_active
) VALUES (
    'admin@doordash.com', 
    'admin', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeANuQxbcD6SJRqj.', -- Admin123!
    'System', 
    'Administrator', 
    true, 
    true
);

-- Assign admin role to default admin user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- Insert sample OAuth providers (disabled by default)
INSERT INTO oauth_providers (
    name, client_id, client_secret, authorization_uri, token_uri, user_info_uri, scope, is_active
) VALUES 
(
    'google', 
    'your-google-client-id', 
    'your-google-client-secret',
    'https://accounts.google.com/o/oauth2/auth',
    'https://oauth2.googleapis.com/token',
    'https://www.googleapis.com/oauth2/v2/userinfo',
    'openid email profile',
    false
),
(
    'facebook', 
    'your-facebook-client-id', 
    'your-facebook-client-secret',
    'https://www.facebook.com/v12.0/dialog/oauth',
    'https://graph.facebook.com/v12.0/oauth/access_token',
    'https://graph.facebook.com/v12.0/me',
    'email',
    false
),
(
    'apple', 
    'your-apple-client-id', 
    'your-apple-client-secret',
    'https://appleid.apple.com/auth/authorize',
    'https://appleid.apple.com/auth/token',
    'https://appleid.apple.com/auth/userinfo',
    'email name',
    false
);
