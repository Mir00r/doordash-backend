#!/bin/bash

# API Gateway Management Script
# This script helps with building, testing, and running the API Gateway

set -e

PROJECT_NAME="doordash-api-gateway"
SERVICE_NAME="api-gateway"
DOCKER_IMAGE="doordash/${SERVICE_NAME}:latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 21 or later."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?\K[0-9]+')
    if [ "$JAVA_VERSION" -lt 21 ]; then
        print_error "Java 21 or later is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    if ! command_exists docker; then
        print_warning "Docker is not installed. Docker commands will not work."
    fi
    
    print_success "Prerequisites check completed."
}

# Function to clean build artifacts
clean() {
    print_status "Cleaning build artifacts..."
    ./gradlew clean
    print_success "Clean completed."
}

# Function to build the application
build() {
    print_status "Building the application..."
    ./gradlew build -x test
    print_success "Build completed."
}

# Function to run tests
test() {
    print_status "Running tests..."
    ./gradlew test
    print_success "Tests completed."
}

# Function to run the application locally
run() {
    print_status "Starting the application locally..."
    print_warning "Make sure Redis, Consul, and Kafka are running locally or update application.yml"
    ./gradlew bootRun
}

# Function to build Docker image
docker_build() {
    print_status "Building Docker image..."
    if ! command_exists docker; then
        print_error "Docker is not installed."
        exit 1
    fi
    
    docker build -t $DOCKER_IMAGE .
    print_success "Docker image built: $DOCKER_IMAGE"
}

# Function to run with Docker Compose
docker_run() {
    print_status "Starting services with Docker Compose..."
    if ! command_exists docker-compose && ! docker compose version >/dev/null 2>&1; then
        print_error "Docker Compose is not available."
        exit 1
    fi
    
    # Use docker compose or docker-compose based on availability
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    $COMPOSE_CMD up -d
    print_success "Services started. API Gateway available at http://localhost:8080"
    print_status "Useful endpoints:"
    echo "  - Health: http://localhost:8080/actuator/health"
    echo "  - API Docs: http://localhost:8080/api-docs"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3000 (admin/admin)"
    echo "  - Zipkin: http://localhost:9411"
}

# Function to stop Docker services
docker_stop() {
    print_status "Stopping Docker services..."
    if docker compose version >/dev/null 2>&1; then
        docker compose down
    else
        docker-compose down
    fi
    print_success "Services stopped."
}

# Function to show logs
logs() {
    if [ "$2" == "docker" ]; then
        print_status "Showing Docker logs..."
        if docker compose version >/dev/null 2>&1; then
            docker compose logs -f api-gateway
        else
            docker-compose logs -f api-gateway
        fi
    else
        print_status "Showing application logs..."
        ./gradlew bootRun --console=plain
    fi
}

# Function to check health
health() {
    print_status "Checking application health..."
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        print_success "Application is healthy!"
        curl -s http://localhost:8080/actuator/health | jq '.' || echo ""
    else
        print_error "Application is not responding or unhealthy."
        exit 1
    fi
}

# Function to show help
show_help() {
    echo "API Gateway Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  clean         Clean build artifacts"
    echo "  build         Build the application"
    echo "  test          Run tests"
    echo "  run           Run the application locally"
    echo "  docker-build  Build Docker image"
    echo "  docker-run    Start services with Docker Compose"
    echo "  docker-stop   Stop Docker services"
    echo "  logs [docker] Show application logs (add 'docker' for container logs)"
    echo "  health        Check application health"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build && $0 test    # Build and test"
    echo "  $0 docker-build && $0 docker-run    # Build and run with Docker"
    echo "  $0 logs docker         # Show Docker container logs"
}

# Main script logic
case "${1:-help}" in
    "clean")
        check_prerequisites
        clean
        ;;
    "build")
        check_prerequisites
        build
        ;;
    "test")
        check_prerequisites
        test
        ;;
    "run")
        check_prerequisites
        build
        run
        ;;
    "docker-build")
        check_prerequisites
        docker_build
        ;;
    "docker-run")
        docker_run
        ;;
    "docker-stop")
        docker_stop
        ;;
    "logs")
        logs "$@"
        ;;
    "health")
        health
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
