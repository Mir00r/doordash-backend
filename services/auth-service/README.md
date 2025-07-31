# Auth Service

A comprehensive authentication and authorization service for the DoorDash backend system.

## Features

- User registration and authentication
- JWT token generation and validation
- Password reset functionality
- Email verification
- OAuth2 integration
- Role-based access control (RBAC)
- Rate limiting for security
- Session management
- Multi-factor authentication (MFA) support
- Audit logging

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/verify-email` - Email verification
- `POST /api/v1/auth/resend-verification` - Resend verification email

### Password Management
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/reset-password` - Reset password with token
- `POST /api/v1/auth/change-password` - Change password (authenticated)

### Token Management
- `GET /api/v1/auth/validate` - Validate JWT token
- `POST /api/v1/auth/revoke` - Revoke JWT token

### User Management
- `GET /api/v1/auth/profile` - Get user profile
- `PUT /api/v1/auth/profile` - Update user profile

## Security Features

- Password strength validation
- Account lockout after failed attempts
- Rate limiting on sensitive endpoints
- JWT token blacklisting
- Secure password hashing with BCrypt
- Email verification for new accounts

## Configuration

Key configuration properties in `application.yml`:
- JWT secret and expiration
- Database connection
- Redis cache configuration
- Email service settings
- Rate limiting settings
