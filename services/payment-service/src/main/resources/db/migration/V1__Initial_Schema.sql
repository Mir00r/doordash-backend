-- Payment Service Database Schema
-- Version: 1.0.0
-- Author: DoorDash Engineering

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create custom types for enums
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'PROCESSING', 
    'SUCCEEDED',
    'FAILED',
    'CANCELLED',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
);

CREATE TYPE payment_provider AS ENUM (
    'STRIPE',
    'PAYPAL',
    'BRAINTREE',
    'APPLE_PAY',
    'GOOGLE_PAY'
);

CREATE TYPE payment_method_type AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'BANK_ACCOUNT',
    'DIGITAL_WALLET',
    'APPLE_PAY',
    'GOOGLE_PAY',
    'PAYPAL'
);

CREATE TYPE verification_status AS ENUM (
    'PENDING',
    'VERIFIED',
    'FAILED',
    'REQUIRES_ACTION'
);

CREATE TYPE refund_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'SUCCEEDED',
    'FAILED',
    'CANCELLED'
);

CREATE TYPE refund_reason AS ENUM (
    'CUSTOMER_REQUEST',
    'ORDER_CANCELLED',
    'DUPLICATE_PAYMENT',
    'FRAUDULENT',
    'MERCHANT_ERROR',
    'PROCESSING_ERROR',
    'CHARGEBACK',
    'OTHER'
);

CREATE TYPE audit_action AS ENUM (
    'CREATE',
    'UPDATE',
    'DELETE',
    'VIEW',
    'PROCESS',
    'AUTHORIZE',
    'CAPTURE',
    'REFUND',
    'CANCEL',
    'SECURITY_EVENT',
    'COMPLIANCE_CHECK',
    'FRAUD_DETECTION'
);

-- Payment Methods Table
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    type payment_method_type NOT NULL,
    provider payment_provider NOT NULL,
    provider_method_id VARCHAR(255) NOT NULL,
    last_four_digits VARCHAR(4),
    brand VARCHAR(50),
    expiry_month INTEGER,
    expiry_year INTEGER,
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(2),
    cardholder_name VARCHAR(255),
    fingerprint VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    verification_status verification_status DEFAULT 'PENDING',
    verification_data JSONB,
    risk_score DECIMAL(3,2),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    order_id UUID NOT NULL,
    payment_method_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status payment_status NOT NULL DEFAULT 'PENDING',
    provider payment_provider NOT NULL,
    provider_transaction_id VARCHAR(255),
    provider_payment_intent_id VARCHAR(255),
    description TEXT,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    processed_at TIMESTAMP WITH TIME ZONE,
    settled_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    risk_score DECIMAL(3,2),
    is_test BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_payments_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

-- Refunds Table
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status refund_status NOT NULL DEFAULT 'PENDING',
    reason refund_reason NOT NULL,
    description TEXT,
    provider_refund_id VARCHAR(255),
    failure_reason TEXT,
    initiated_by UUID NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Audit Logs Table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action audit_action NOT NULL,
    user_id UUID,
    user_type VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    description TEXT,
    risk_indicators JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_logs_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Create indexes for performance
CREATE INDEX idx_payment_method_user_id ON payment_methods(user_id);
CREATE INDEX idx_payment_method_provider ON payment_methods(provider);
CREATE INDEX idx_payment_method_type ON payment_methods(type);
CREATE INDEX idx_payment_method_is_default ON payment_methods(is_default);
CREATE INDEX idx_payment_method_fingerprint ON payment_methods(fingerprint);
CREATE UNIQUE INDEX idx_payment_method_provider_method_id ON payment_methods(provider_method_id);

CREATE INDEX idx_payment_user_id ON payments(user_id);
CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_provider ON payments(provider);
CREATE INDEX idx_payment_created_at ON payments(created_at);
CREATE INDEX idx_payment_processed_at ON payments(processed_at);
CREATE INDEX idx_payment_provider_transaction_id ON payments(provider_transaction_id);
CREATE INDEX idx_payment_provider_payment_intent_id ON payments(provider_payment_intent_id);
CREATE UNIQUE INDEX idx_payment_order_id_unique ON payments(order_id);

CREATE INDEX idx_refund_payment_id ON refunds(payment_id);
CREATE INDEX idx_refund_status ON refunds(status);
CREATE INDEX idx_refund_created_at ON refunds(created_at);
CREATE INDEX idx_refund_provider_refund_id ON refunds(provider_refund_id);

CREATE INDEX idx_audit_log_payment_id ON audit_logs(payment_id);
CREATE INDEX idx_audit_log_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_log_action ON audit_logs(action);
CREATE INDEX idx_audit_log_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_log_entity_id ON audit_logs(entity_id);

-- Add triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_payment_methods_updated_at 
    BEFORE UPDATE ON payment_methods 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at 
    BEFORE UPDATE ON payments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_refunds_updated_at 
    BEFORE UPDATE ON refunds 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add constraints
ALTER TABLE payment_methods ADD CONSTRAINT chk_payment_method_amount_positive 
    CHECK (risk_score IS NULL OR (risk_score >= 0 AND risk_score <= 1));

ALTER TABLE payments ADD CONSTRAINT chk_payment_amount_positive 
    CHECK (amount > 0);

ALTER TABLE payments ADD CONSTRAINT chk_payment_retry_count_positive 
    CHECK (retry_count >= 0);

ALTER TABLE payments ADD CONSTRAINT chk_payment_risk_score_range 
    CHECK (risk_score IS NULL OR (risk_score >= 0 AND risk_score <= 1));

ALTER TABLE refunds ADD CONSTRAINT chk_refund_amount_positive 
    CHECK (amount > 0);

-- Add comments for documentation
COMMENT ON TABLE payment_methods IS 'Stores encrypted payment method information with PCI DSS compliance';
COMMENT ON TABLE payments IS 'Core payment transactions with comprehensive audit trail';
COMMENT ON TABLE refunds IS 'Refund transactions linked to original payments';
COMMENT ON TABLE audit_logs IS 'Complete audit trail for all payment operations';

COMMENT ON COLUMN payment_methods.provider_method_id IS 'Tokenized payment method ID from payment provider';
COMMENT ON COLUMN payment_methods.fingerprint IS 'Unique fingerprint for duplicate detection';
COMMENT ON COLUMN payments.provider_transaction_id IS 'Transaction ID from payment provider';
COMMENT ON COLUMN payments.risk_score IS 'Calculated risk score between 0 and 1';
COMMENT ON COLUMN audit_logs.risk_indicators IS 'JSON object containing risk assessment data';
