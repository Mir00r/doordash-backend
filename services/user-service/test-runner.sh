#!/bin/bash

# Enhanced Test Execution Script for User Service
# This script provides convenient commands for running different types of tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_color() {
    printf "${1}${2}${NC}\n"
}

print_header() {
    echo
    print_color $BLUE "=================================================="
    print_color $BLUE "$1"
    print_color $BLUE "=================================================="
    echo
}

print_success() {
    print_color $GREEN "✅ $1"
}

print_warning() {
    print_color $YELLOW "⚠️  $1"
}

print_error() {
    print_color $RED "❌ $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker Desktop."
        exit 1
    fi
}

# Function to clean up Docker containers
cleanup_containers() {
    print_header "Cleaning up test containers"
    docker system prune -f --volumes 2>/dev/null || true
    print_success "Container cleanup completed"
}

# Function to run unit tests
run_unit_tests() {
    print_header "Running Unit Tests"
    ./gradlew clean unitTest --info
    print_success "Unit tests completed"
}

# Function to run integration tests
run_integration_tests() {
    print_header "Running Integration Tests"
    check_docker
    ./gradlew clean integrationTest --info
    print_success "Integration tests completed"
}

# Function to run contract tests
run_contract_tests() {
    print_header "Running Contract Tests"
    ./gradlew clean contractTest --info
    print_success "Contract tests completed"
}

# Function to run E2E tests
run_e2e_tests() {
    print_header "Running End-to-End Tests"
    check_docker
    ./gradlew clean e2eTest --info
    print_success "E2E tests completed"
}

# Function to run performance tests
run_performance_tests() {
    print_header "Running Performance Tests"
    check_docker
    print_warning "Performance tests may take several minutes to complete"
    ./gradlew clean performanceTest --info
    print_success "Performance tests completed"
}

# Function to run security tests
run_security_tests() {
    print_header "Running Security Tests"
    check_docker
    ./gradlew clean securityTest --info
    print_success "Security tests completed"
}

# Function to run all tests
run_all_tests() {
    print_header "Running All Tests"
    check_docker
    print_warning "This will take a significant amount of time"
    
    ./gradlew clean allTests --info
    print_success "All tests completed successfully!"
}

# Function to run tests with coverage
run_tests_with_coverage() {
    print_header "Running Tests with Code Coverage"
    check_docker
    ./gradlew clean testWithCoverage --info
    
    # Open coverage report if on macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open build/jacoco/html/index.html 2>/dev/null || true
    fi
    
    print_success "Tests with coverage completed"
    print_color $BLUE "Coverage report available at: build/jacoco/html/index.html"
}

# Function to run quick tests for development
run_quick_tests() {
    print_header "Running Quick Tests (Unit + Smoke)"
    ./gradlew clean quickTest --info
    print_success "Quick tests completed"
}

# Function to validate test setup
validate_setup() {
    print_header "Validating Test Setup"
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_color $BLUE "Java Version: $JAVA_VERSION"
    
    # Check Gradle version
    GRADLE_VERSION=$(./gradlew --version | grep "Gradle" | cut -d' ' -f2)
    print_color $BLUE "Gradle Version: $GRADLE_VERSION"
    
    # Check Docker
    if docker --version >/dev/null 2>&1; then
        DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | cut -d',' -f1)
        print_color $BLUE "Docker Version: $DOCKER_VERSION"
    else
        print_warning "Docker not available - integration tests may fail"
    fi
    
    # Test database connectivity
    print_color $BLUE "Testing database connectivity..."
    ./gradlew flywayInfo >/dev/null 2>&1 && print_success "Database connection OK" || print_warning "Database connection issues detected"
    
    print_success "Setup validation completed"
}

# Function to show help
show_help() {
    cat << EOF
Enhanced Test Script for User Service

Usage: $0 [COMMAND]

Available Commands:
  unit           Run unit tests only
  integration    Run integration tests (requires Docker)
  contract       Run contract tests
  e2e            Run end-to-end tests (requires Docker)
  performance    Run performance tests (requires Docker)
  security       Run security tests (requires Docker)
  all            Run all test types (requires Docker)
  coverage       Run tests with code coverage report
  quick          Run quick tests for development (unit + smoke)
  validate       Validate test environment setup
  cleanup        Clean up Docker containers and volumes
  help           Show this help message

Examples:
  $0 unit                 # Run only unit tests
  $0 integration         # Run integration tests
  $0 coverage            # Run tests with coverage
  $0 all                 # Run complete test suite

Notes:
  - Integration, E2E, Performance, and Security tests require Docker
  - Performance tests may take 10+ minutes to complete
  - Coverage reports are generated in build/jacoco/html/
  - Test containers are automatically managed via Testcontainers

For more information, see: README.md
EOF
}

# Main script logic
case "${1:-help}" in
    "unit")
        run_unit_tests
        ;;
    "integration")
        run_integration_tests
        ;;
    "contract")
        run_contract_tests
        ;;
    "e2e")
        run_e2e_tests
        ;;
    "performance")
        run_performance_tests
        ;;
    "security")
        run_security_tests
        ;;
    "all")
        run_all_tests
        ;;
    "coverage")
        run_tests_with_coverage
        ;;
    "quick")
        run_quick_tests
        ;;
    "validate")
        validate_setup
        ;;
    "cleanup")
        cleanup_containers
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo
        show_help
        exit 1
        ;;
esac
