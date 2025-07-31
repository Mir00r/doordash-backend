# Comprehensive Testing Guide for User Service

## Overview

The User Service implements a comprehensive testing strategy following industry best practices, covering all aspects of the application from unit tests to end-to-end scenarios. This document outlines the testing approach, available test types, and execution procedures.

## Testing Philosophy

Our testing strategy follows the testing pyramid approach:

1. **Unit Tests (70%)** - Fast, isolated tests for business logic
2. **Integration Tests (20%)** - Component interaction validation
3. **End-to-End Tests (10%)** - Complete workflow validation

## Test Types and Coverage

### 1. Unit Tests (`@Tag("unit")`)

**Purpose**: Test individual components in isolation using mocks and stubs.

**Coverage**:
- Service layer business logic
- Validation logic
- Utility functions
- Domain object behavior
- Exception handling

**Location**: `src/test/java/.../unit/`

**Execution**: 
```bash
./test-runner.sh unit
# or
./gradlew unitTest
```

**Features**:
- Fast execution (< 2 minutes)
- No external dependencies
- High code coverage requirements (85%+)
- Comprehensive edge case testing

### 2. Integration Tests (`@Tag("integration")`)

**Purpose**: Test component interactions with real infrastructure using Testcontainers.

**Coverage**:
- Database interactions
- Cache behavior
- Message queue operations
- REST API endpoints
- Security configurations

**Location**: `src/test/java/.../integration/`

**Infrastructure**:
- PostgreSQL container
- Redis container
- Kafka container

**Execution**:
```bash
./test-runner.sh integration
# or
./gradlew integrationTest
```

**Features**:
- Real database operations
- Cache integration testing
- Event-driven architecture validation
- Security context testing

### 3. Contract Tests (`@Tag("contract")`)

**Purpose**: Ensure API compatibility between microservices using Spring Cloud Contract.

**Coverage**:
- API request/response structures
- HTTP status codes
- Data validation contracts
- Error response formats
- Backward compatibility

**Location**: `src/test/resources/contracts/`

**Execution**:
```bash
./test-runner.sh contract
# or
./gradlew contractTest
```

**Features**:
- Consumer-driven contract testing
- Stub generation for consumer tests
- API versioning compatibility
- Contract repository integration

### 4. End-to-End Tests (`@Tag("e2e")`)

**Purpose**: Validate complete user workflows from start to finish.

**Coverage**:
- Complete CRUD operations
- Authentication/authorization flows
- Data validation workflows
- Cache behavior verification
- Concurrent operations

**Location**: `src/test/java/.../e2e/`

**Execution**:
```bash
./test-runner.sh e2e
# or
./gradlew e2eTest
```

**Features**:
- Full application context
- Real database and cache
- Complete user journeys
- Cross-cutting concern validation

### 5. Performance Tests (`@Tag("performance")`)

**Purpose**: Validate application performance under various load conditions.

**Coverage**:
- Load testing (1000+ concurrent requests)
- Throughput measurement
- Response time analysis
- Memory usage monitoring
- Cache performance validation
- Scalability testing

**Location**: `src/test/java/.../performance/`

**Execution**:
```bash
./test-runner.sh performance
# or
./gradlew performanceTest
```

**Performance Targets**:
- Throughput: > 100 RPS
- Response time: < 500ms (95th percentile)
- Memory usage: < 50% increase under load
- Cache hit ratio: > 80%

### 6. Security Tests (`@Tag("security")`)

**Purpose**: Validate security controls and prevent vulnerabilities.

**Coverage**:
- Authentication/authorization
- Input validation and sanitization
- SQL injection prevention
- XSS prevention
- CSRF protection
- Rate limiting
- Security headers

**Location**: `src/test/java/.../security/`

**Execution**:
```bash
./test-runner.sh security
# or
./gradlew securityTest
```

**Security Scenarios**:
- Malicious input handling
- Authentication bypass attempts
- Authorization boundary testing
- Session management validation

## Enhanced Testing Features

### 1. Enhanced Test Data Factory

**Class**: `EnhancedTestDataFactory`

**Features**:
- Realistic test data generation using Faker
- Edge case data scenarios
- Performance test data sets
- Security testing payloads
- Bulk data generation

**Usage**:
```java
// Realistic user creation
User user = EnhancedTestDataFactory.createRealisticUser();

// Bulk data for performance tests
List<User> users = EnhancedTestDataFactory.createRealisticUsers(1000);

// Malicious data for security tests
CreateUserProfileRequest malicious = EnhancedTestDataFactory.createMaliciousRequest();
```

### 2. Enhanced Test Configuration

**Class**: `EnhancedTestConfig`

**Features**:
- Profile-specific configurations
- Mock security context
- Testcontainers integration
- Performance optimizations

**Profiles**:
- `test` - Basic test configuration
- `integration` - Integration test setup
- `performance` - Performance optimized settings
- `security` - Security-focused configuration
- `contract` - Contract testing setup

### 3. Advanced Base Integration Test

**Class**: `EnhancedBaseIntegrationTest`

**Features**:
- Shared container instances
- Parallel container startup
- Health check validation
- Dynamic property configuration
- Resource optimization

**Benefits**:
- Faster test execution
- Consistent test environment
- Reduced resource usage
- Better debugging capabilities

## Test Execution

### Quick Commands

```bash
# Quick development tests
./test-runner.sh quick

# All tests
./test-runner.sh all

# Tests with coverage
./test-runner.sh coverage

# Validate setup
./test-runner.sh validate
```

### Gradle Tasks

```bash
# Individual test types
./gradlew unitTest
./gradlew integrationTest
./gradlew contractTest
./gradlew e2eTest
./gradlew performanceTest
./gradlew securityTest

# Combined executions
./gradlew allTests
./gradlew testWithCoverage

# Coverage tasks
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification
```

## Code Coverage

### Requirements

- **Instruction Coverage**: 85% minimum
- **Branch Coverage**: 75% minimum
- **Line Coverage**: 80% minimum
- **Class Coverage**: Maximum 3 uncovered classes
- **Method Coverage**: Maximum 10 uncovered methods

### Exclusions

The following are excluded from coverage calculations:
- Configuration classes
- DTO/Entity classes
- Enums
- Exception classes
- Mappers
- Application entry points

### Reports

Coverage reports are generated in multiple formats:
- **HTML**: `build/jacoco/html/index.html`
- **XML**: `build/jacoco/jacocoTestReport.xml`
- **CSV**: `build/jacoco/jacocoTestReport.csv`

## Test Configuration Files

### Application Profiles

- `application-enhanced-test.yml` - Enhanced test configuration
- `application-security-test.yml` - Security test configuration
- `application-performance-test.yml` - Performance test settings

### Contract Definitions

- `src/test/resources/contracts/user/` - Contract definitions in Groovy DSL

## Best Practices

### Test Organization

1. **Follow AAA Pattern**: Arrange, Act, Assert
2. **Use Descriptive Test Names**: Clearly indicate what is being tested
3. **Group Related Tests**: Use `@Nested` classes for logical grouping
4. **Tag Tests Appropriately**: Use `@Tag` for test categorization

### Test Data Management

1. **Use Factory Methods**: Leverage `EnhancedTestDataFactory` for consistent data
2. **Clean Up Resources**: Ensure proper cleanup between tests
3. **Avoid Test Dependencies**: Each test should be independent
4. **Use Realistic Data**: Prefer realistic test data over simplistic examples

### Performance Considerations

1. **Reuse Containers**: Leverage Testcontainers reuse feature
2. **Parallel Execution**: Enable parallel test execution where safe
3. **Optimize Imports**: Avoid unnecessary dependencies in test context
4. **Cache Test Resources**: Reuse expensive-to-create test resources

### Security Testing

1. **Test Input Validation**: Verify all user inputs are properly validated
2. **Check Authorization**: Ensure proper access controls
3. **Validate Sanitization**: Confirm malicious input is safely handled
4. **Test Rate Limiting**: Verify protective measures are effective

## Continuous Integration

### CI Pipeline Integration

```yaml
# Example GitHub Actions workflow
- name: Run Unit Tests
  run: ./gradlew unitTest

- name: Run Integration Tests
  run: ./gradlew integrationTest

- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport

- name: Upload Coverage
  uses: codecov/codecov-action@v3
  with:
    file: build/jacoco/jacocoTestReport.xml
```

### Quality Gates

1. **Coverage Threshold**: 85% instruction coverage required
2. **Test Stability**: No flaky tests allowed
3. **Performance Regression**: Response time increases > 20% fail build
4. **Security Validation**: All security tests must pass

## Troubleshooting

### Common Issues

1. **Container Startup Failures**
   - Ensure Docker is running
   - Check available memory (8GB+ recommended)
   - Verify port availability

2. **Test Timeouts**
   - Increase timeout values for slower environments
   - Check container resource allocation
   - Verify network connectivity

3. **Memory Issues**
   - Increase JVM heap size for tests
   - Enable container reuse
   - Clean up test data between runs

4. **Flaky Tests**
   - Add proper wait conditions
   - Use Awaitility for asynchronous operations
   - Ensure test isolation

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.doordash.user_service: DEBUG
    org.testcontainers: DEBUG
```

## Monitoring and Metrics

### Test Metrics

- Test execution time trends
- Coverage percentage over time
- Flaky test identification
- Performance regression detection

### Performance Baselines

- Response time percentiles
- Throughput measurements
- Memory usage patterns
- Cache hit ratios

## Future Enhancements

1. **Chaos Engineering**: Add fault injection testing
2. **Load Testing**: Implement continuous load testing
3. **API Compatibility**: Automated breaking change detection
4. **Visual Testing**: UI component regression testing
5. **Accessibility Testing**: WCAG compliance validation

## Conclusion

This comprehensive testing strategy ensures the User Service maintains high quality, performance, and security standards. The multi-layered approach provides confidence in the application's reliability while enabling rapid development and deployment cycles.

For questions or suggestions regarding the testing approach, please contact the DoorDash Backend Team.
