-- Insert default notification templates
INSERT INTO notification_templates (name, type, subject_template, content_template, variables, created_by) VALUES
-- Order Templates
('order-confirmation-email', 'EMAIL', 'Order Confirmation - Order #{{orderNumber}}', 
'<html><body><h1>Thank you for your order!</h1><p>Your order #{{orderNumber}} has been confirmed.</p><p>Total: ${{totalAmount}}</p><p>Estimated delivery: {{estimatedDelivery}}</p></body></html>',
'{"orderNumber": "string", "totalAmount": "number", "estimatedDelivery": "string", "customerName": "string"}', 1),

('order-confirmed-sms', 'SMS', null, 
'Your DoorDash order #{{orderNumber}} is confirmed! Estimated delivery: {{estimatedDelivery}}. Track your order in the app.',
'{"orderNumber": "string", "estimatedDelivery": "string"}', 1),

('order-confirmed-push', 'PUSH', 'Order Confirmed', 
'Your order #{{orderNumber}} is confirmed! Estimated delivery: {{estimatedDelivery}}',
'{"orderNumber": "string", "estimatedDelivery": "string"}', 1),

-- Delivery Templates
('order-preparing-email', 'EMAIL', 'Your order is being prepared - Order #{{orderNumber}}',
'<html><body><h1>Your order is being prepared</h1><p>Good news! {{restaurantName}} has started preparing your order #{{orderNumber}}.</p><p>Estimated ready time: {{estimatedReadyTime}}</p></body></html>',
'{"orderNumber": "string", "restaurantName": "string", "estimatedReadyTime": "string"}', 1),

('order-preparing-sms', 'SMS', null,
'{{restaurantName}} is preparing your order #{{orderNumber}}. Ready in ~{{estimatedReadyTime}}',
'{"orderNumber": "string", "restaurantName": "string", "estimatedReadyTime": "string"}', 1),

('order-preparing-push', 'PUSH', 'Order Being Prepared',
'{{restaurantName}} is preparing your order #{{orderNumber}}',
'{"orderNumber": "string", "restaurantName": "string"}', 1),

('order-ready-email', 'EMAIL', 'Your order is ready for pickup - Order #{{orderNumber}}',
'<html><body><h1>Your order is ready!</h1><p>Your order #{{orderNumber}} from {{restaurantName}} is ready for pickup.</p><p>Driver: {{driverName}} - {{driverPhone}}</p></body></html>',
'{"orderNumber": "string", "restaurantName": "string", "driverName": "string", "driverPhone": "string"}', 1),

('order-ready-sms', 'SMS', null,
'Your order #{{orderNumber}} is ready! Driver {{driverName}} is on the way. Track delivery in the app.',
'{"orderNumber": "string", "driverName": "string"}', 1),

('order-ready-push', 'PUSH', 'Order Ready',
'Your order #{{orderNumber}} is ready! Driver {{driverName}} is on the way.',
'{"orderNumber": "string", "driverName": "string"}', 1),

('order-out-for-delivery-email', 'EMAIL', 'Your order is out for delivery - Order #{{orderNumber}}',
'<html><body><h1>Your order is on the way!</h1><p>Your order #{{orderNumber}} is out for delivery.</p><p>Driver: {{driverName}}</p><p>Estimated arrival: {{estimatedArrival}}</p><p>Track your delivery: <a href="{{trackingUrl}}">Click here</a></p></body></html>',
'{"orderNumber": "string", "driverName": "string", "estimatedArrival": "string", "trackingUrl": "string"}', 1),

('order-out-for-delivery-sms', 'SMS', null,
'Your order #{{orderNumber}} is out for delivery! Driver {{driverName}} will arrive in ~{{estimatedArrival}}. Track: {{trackingUrl}}',
'{"orderNumber": "string", "driverName": "string", "estimatedArrival": "string", "trackingUrl": "string"}', 1),

('order-out-for-delivery-push', 'PUSH', 'Out for Delivery',
'Your order #{{orderNumber}} is on the way! Arriving in ~{{estimatedArrival}}',
'{"orderNumber": "string", "estimatedArrival": "string"}', 1),

('order-delivered-email', 'EMAIL', 'Your order has been delivered - Order #{{orderNumber}}',
'<html><body><h1>Order Delivered!</h1><p>Your order #{{orderNumber}} has been delivered.</p><p>Delivered at: {{deliveredAt}}</p><p>Thanks for choosing DoorDash!</p><p><a href="{{ratingUrl}}">Rate your experience</a></p></body></html>',
'{"orderNumber": "string", "deliveredAt": "string", "ratingUrl": "string"}', 1),

('order-delivered-sms', 'SMS', null,
'Your order #{{orderNumber}} has been delivered! Enjoy your meal and rate your experience in the app.',
'{"orderNumber": "string"}', 1),

('order-delivered-push', 'PUSH', 'Order Delivered',
'Your order #{{orderNumber}} has been delivered! Enjoy your meal!',
'{"orderNumber": "string"}', 1),

-- Payment Templates
('payment-processed-email', 'EMAIL', 'Payment Confirmation - Order #{{orderNumber}}',
'<html><body><h1>Payment Processed</h1><p>Your payment of ${{amount}} for order #{{orderNumber}} has been processed successfully.</p><p>Payment method: {{paymentMethod}}</p><p>Transaction ID: {{transactionId}}</p></body></html>',
'{"orderNumber": "string", "amount": "number", "paymentMethod": "string", "transactionId": "string"}', 1),

('payment-failed-email', 'EMAIL', 'Payment Failed - Order #{{orderNumber}}',
'<html><body><h1>Payment Failed</h1><p>We were unable to process your payment for order #{{orderNumber}}.</p><p>Reason: {{failureReason}}</p><p>Please update your payment method and try again.</p></body></html>',
'{"orderNumber": "string", "failureReason": "string"}', 1),

('payment-failed-sms', 'SMS', null,
'Payment failed for order #{{orderNumber}}. Please update your payment method in the app to complete your order.',
'{"orderNumber": "string"}', 1),

('payment-failed-push', 'PUSH', 'Payment Failed',
'Payment failed for order #{{orderNumber}}. Please update your payment method.',
'{"orderNumber": "string"}', 1),

-- User Account Templates
('welcome-email', 'EMAIL', 'Welcome to DoorDash!',
'<html><body><h1>Welcome to DoorDash, {{firstName}}!</h1><p>Thank you for joining us. Get ready to discover amazing food from your favorite restaurants.</p><p>Use code WELCOME10 for 10% off your first order!</p></body></html>',
'{"firstName": "string"}', 1),

('password-reset-email', 'EMAIL', 'Reset Your DoorDash Password',
'<html><body><h1>Password Reset</h1><p>Hi {{firstName}},</p><p>Click the link below to reset your password:</p><p><a href="{{resetUrl}}">Reset Password</a></p><p>This link expires in 1 hour.</p></body></html>',
'{"firstName": "string", "resetUrl": "string"}', 1),

('email-verification-email', 'EMAIL', 'Verify Your Email Address',
'<html><body><h1>Verify Your Email</h1><p>Hi {{firstName}},</p><p>Please verify your email address by clicking the link below:</p><p><a href="{{verificationUrl}}">Verify Email</a></p></body></html>',
'{"firstName": "string", "verificationUrl": "string"}', 1),

-- Driver Templates
('delivery-assignment-sms', 'SMS', null,
'New delivery assigned! Order #{{orderNumber}} from {{restaurantName}} to {{customerAddress}}. Pickup by {{pickupTime}}.',
'{"orderNumber": "string", "restaurantName": "string", "customerAddress": "string", "pickupTime": "string"}', 1),

('delivery-assignment-push', 'PUSH', 'New Delivery',
'New delivery assigned! Order #{{orderNumber}} from {{restaurantName}}',
'{"orderNumber": "string", "restaurantName": "string"}', 1),

-- Restaurant Templates
('new-order-sms', 'SMS', null,
'New order received! Order #{{orderNumber}} - ${{totalAmount}}. Prepare by {{prepareByTime}}.',
'{"orderNumber": "string", "totalAmount": "number", "prepareByTime": "string"}', 1),

('new-order-push', 'PUSH', 'New Order',
'New order #{{orderNumber}} - ${{totalAmount}}',
'{"orderNumber": "string", "totalAmount": "number"}', 1),

-- Marketing Templates
('promotion-email', 'EMAIL', 'Special Offer Just for You!',
'<html><body><h1>{{promotionTitle}}</h1><p>{{promotionDescription}}</p><p>Use code: {{promoCode}}</p><p>Valid until: {{expiryDate}}</p></body></html>',
'{"promotionTitle": "string", "promotionDescription": "string", "promoCode": "string", "expiryDate": "string"}', 1),

('promotion-push', 'PUSH', '{{promotionTitle}}',
'{{promotionDescription}} Use code: {{promoCode}}',
'{"promotionTitle": "string", "promotionDescription": "string", "promoCode": "string"}', 1);

-- Insert default user notification preferences for system user (id: 1)
INSERT INTO user_notification_preferences (user_id, email_enabled, sms_enabled, push_enabled, in_app_enabled, marketing_emails, order_updates, delivery_updates, promotional_notifications, time_zone, preferred_language) VALUES
(1, true, true, true, true, false, true, true, false, 'UTC', 'en');

-- Insert sample device tokens for testing
INSERT INTO device_tokens (user_id, device_id, token, platform, app_version, is_active) VALUES
(1, 'device_001', 'fcm_token_sample_001', 'android', '1.0.0', true),
(1, 'device_002', 'fcm_token_sample_002', 'ios', '1.0.0', true),
(2, 'device_003', 'fcm_token_sample_003', 'web', '1.0.0', true);
