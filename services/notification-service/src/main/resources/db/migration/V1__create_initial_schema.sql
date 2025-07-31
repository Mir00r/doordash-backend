-- Create notification schema
CREATE SCHEMA IF NOT EXISTS notification;

-- Create enum types
CREATE TYPE notification_type AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBSOCKET');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'CANCELLED');
CREATE TYPE template_type AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP');
CREATE TYPE provider_type AS ENUM ('SENDGRID', 'SMTP', 'TWILIO', 'FCM', 'APNS', 'WEBSOCKET');
CREATE TYPE delivery_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED', 'CLICKED', 'OPENED');

-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type notification_type NOT NULL,
    channel VARCHAR(50) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    template_id BIGINT,
    context_data JSONB,
    priority INTEGER DEFAULT 5,
    status notification_status DEFAULT 'PENDING',
    provider_type provider_type,
    provider_message_id VARCHAR(255),
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_templates table
CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type template_type NOT NULL,
    subject_template VARCHAR(500),
    content_template TEXT NOT NULL,
    variables JSONB,
    is_active BOOLEAN DEFAULT true,
    version INTEGER DEFAULT 1,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_notification_preferences table
CREATE TABLE user_notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    in_app_enabled BOOLEAN DEFAULT true,
    marketing_emails BOOLEAN DEFAULT false,
    order_updates BOOLEAN DEFAULT true,
    delivery_updates BOOLEAN DEFAULT true,
    promotional_notifications BOOLEAN DEFAULT false,
    time_zone VARCHAR(50) DEFAULT 'UTC',
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    preferred_language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_delivery_logs table
CREATE TABLE notification_delivery_logs (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    provider_type provider_type NOT NULL,
    provider_message_id VARCHAR(255),
    delivery_status delivery_status NOT NULL,
    response_code VARCHAR(50),
    response_message TEXT,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    unsubscribed_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_events table for analytics
CREATE TABLE notification_events (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    user_agent TEXT,
    ip_address INET,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create device_tokens table for push notifications
CREATE TABLE device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    token VARCHAR(500) NOT NULL,
    platform VARCHAR(20) NOT NULL, -- 'ios', 'android', 'web'
    app_version VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, device_id)
);

-- Create notification_batches table for bulk operations
CREATE TABLE notification_batches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    template_id BIGINT REFERENCES notification_templates(id),
    target_criteria JSONB,
    total_recipients INTEGER DEFAULT 0,
    sent_count INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_provider_message_id ON notifications(provider_message_id);

CREATE INDEX idx_notification_delivery_logs_notification_id ON notification_delivery_logs(notification_id);
CREATE INDEX idx_notification_delivery_logs_provider_message_id ON notification_delivery_logs(provider_message_id);
CREATE INDEX idx_notification_delivery_logs_delivery_status ON notification_delivery_logs(delivery_status);

CREATE INDEX idx_notification_events_notification_id ON notification_events(notification_id);
CREATE INDEX idx_notification_events_event_type ON notification_events(event_type);
CREATE INDEX idx_notification_events_created_at ON notification_events(created_at);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_token ON device_tokens(token);
CREATE INDEX idx_device_tokens_platform ON device_tokens(platform);

CREATE INDEX idx_user_notification_preferences_user_id ON user_notification_preferences(user_id);

CREATE INDEX idx_notification_templates_name ON notification_templates(name);
CREATE INDEX idx_notification_templates_type ON notification_templates(type);
CREATE INDEX idx_notification_templates_is_active ON notification_templates(is_active);

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_templates_updated_at BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_notification_preferences_updated_at BEFORE UPDATE ON user_notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_device_tokens_updated_at BEFORE UPDATE ON device_tokens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_batches_updated_at BEFORE UPDATE ON notification_batches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
