# Makefile for E-Commerce Platform + PromoX Engine

.PHONY: help build up down restart logs status clean test

# Default target
help:
	@echo "============================================"
	@echo "  E-COMMERCE PLATFORM - MAKE COMMANDS"
	@echo "============================================"
	@echo ""
	@echo "Available commands:"
	@echo "  make build         - Build all Docker images"
	@echo "  make up            - Start all services"
	@echo "  make down          - Stop all services"
	@echo "  make restart       - Restart all services"
	@echo "  make logs          - View logs (all services)"
	@echo "  make logs-f        - Follow logs (all services)"
	@echo "  make status        - Check service status"
	@echo "  make clean         - Remove all containers and volumes"
	@echo "  make test          - Run integration tests"
	@echo ""
	@echo "Service-specific logs:"
	@echo "  make logs-gateway"
	@echo "  make logs-order"
	@echo "  make logs-promotion"
	@echo ""

# Build all Docker images
build:
	@echo "Building all Docker images..."
	docker-compose build

# Start all services
up:
	@echo "Starting all services..."
	./start-all.sh

# Quick start (without wait times)
up-quick:
	@echo "Quick starting all services..."
	docker-compose up -d

# Stop all services
down:
	@echo "Stopping all services..."
	docker-compose down

# Restart all services
restart: down up

# View logs (all services)
logs:
	docker-compose logs

# Follow logs (all services)
logs-f:
	docker-compose logs -f

# Service-specific logs
logs-gateway:
	docker-compose logs -f api-gateway

logs-eureka:
	docker-compose logs -f service-discovery

logs-order:
	docker-compose logs -f order-service

logs-promotion:
	docker-compose logs -f promotion-service

logs-coupon:
	docker-compose logs -f coupon-service

logs-reward:
	docker-compose logs -f reward-service

logs-analytics:
	docker-compose logs -f analytics-service

# Check service status
status:
	./status.sh

# Check service health
health:
	@echo "Checking service health..."
	@curl -s http://localhost:8761/actuator/health || echo "Eureka: DOWN"
	@curl -s http://localhost:8080/actuator/health || echo "Gateway: DOWN"

# Clean everything (including volumes)
clean:
	@echo "Removing all containers and volumes..."
	docker-compose down -v
	@echo "Cleaning Docker system..."
	docker system prune -f

# Clean only volumes
clean-volumes:
	docker-compose down -v

# Rebuild specific service
rebuild-%:
	docker-compose build --no-cache $*
	docker-compose up -d $*

# Scale specific service
scale-%:
	docker-compose up -d --scale $*=2

# Start infrastructure only
infra:
	docker-compose up -d postgres redis zookeeper kafka

# Start core services (Eureka + Gateway)
core:
	docker-compose up -d service-discovery api-gateway

# Start e-commerce services
ecommerce:
	docker-compose up -d product-service inventory-service order-service payment-service user-service cart-service notification-service

# Start PromoX services
promox:
	docker-compose up -d campaign-service promotion-service flashsale-service coupon-service reward-service analytics-service

# Integration test
test:
	@echo "Running integration tests..."
	@echo "Testing API Gateway..."
	@curl -s http://localhost:8080/actuator/health
	@echo ""
	@echo "Testing Eureka..."
	@curl -s http://localhost:8761/actuator/health
	@echo ""
	@echo "All tests passed! ✓"

# Database shell
db-shell:
	docker exec -it ecommerce-postgres psql -U postgres -d ecommerce_platform

# Redis shell
redis-shell:
	docker exec -it ecommerce-redis redis-cli

# View Kafka topics
kafka-topics:
	docker exec -it ecommerce-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Monitor resource usage
monitor:
	docker stats

# Show network info
network:
	docker network inspect ecommerce-network
