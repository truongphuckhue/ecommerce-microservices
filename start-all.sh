#!/bin/bash

echo "============================================"
echo "  E-COMMERCE PLATFORM - STARTUP SCRIPT"
echo "============================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_message "Docker is running ✓"

# Create network if it doesn't exist
print_message "Creating Docker network..."
docker network create ecommerce-network 2>/dev/null || true

# Start infrastructure services first
print_message "Starting infrastructure services (PostgreSQL, Redis, Kafka)..."
docker-compose up -d postgres redis zookeeper kafka

print_message "Waiting for infrastructure to be ready (30 seconds)..."
sleep 30

# Start Eureka Server
print_message "Starting Service Discovery (Eureka)..."
docker-compose up -d service-discovery

print_message "Waiting for Eureka to be ready (20 seconds)..."
sleep 20

# Start API Gateway
print_message "Starting API Gateway..."
docker-compose up -d api-gateway

print_message "Waiting for API Gateway to be ready (15 seconds)..."
sleep 15

# Start E-Commerce Services
print_message "Starting E-Commerce Services..."
docker-compose up -d product-service inventory-service order-service payment-service user-service cart-service notification-service

print_message "Waiting for E-Commerce Services to register (30 seconds)..."
sleep 30

# Start PromoX Services
print_message "Starting PromoX Engine Services..."
docker-compose up -d campaign-service promotion-service flashsale-service coupon-service reward-service analytics-service

print_message "Waiting for PromoX Services to register (30 seconds)..."
sleep 30

echo ""
echo "============================================"
echo "  🎉 ALL SERVICES STARTED SUCCESSFULLY! 🎉"
echo "============================================"
echo ""
echo "Access Points:"
echo "  📊 Eureka Dashboard:  http://localhost:8761"
echo "  🌐 API Gateway:       http://localhost:8080"
echo "  📝 PostgreSQL:        localhost:5432"
echo "  💾 Redis:             localhost:6379"
echo "  📨 Kafka:             localhost:9092"
echo ""
echo "Service Status:"
echo "  Run: docker-compose ps"
echo ""
echo "View Logs:"
echo "  Run: docker-compose logs -f [service-name]"
echo ""
echo "Stop All:"
echo "  Run: ./stop-all.sh"
echo ""
echo "============================================"
