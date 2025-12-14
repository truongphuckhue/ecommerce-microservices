#!/bin/bash

echo "============================================"
echo "  SERVICE STATUS"
echo "============================================"
echo ""

docker-compose ps

echo ""
echo "============================================"
echo "  HEALTH CHECKS"
echo "============================================"
echo ""

# Check Eureka
echo -n "Eureka Server: "
curl -s http://localhost:8761/actuator/health | grep -q '"status":"UP"' && echo "✓ UP" || echo "✗ DOWN"

# Check API Gateway
echo -n "API Gateway: "
curl -s http://localhost:8080/actuator/health | grep -q '"status":"UP"' && echo "✓ UP" || echo "✗ DOWN"

echo ""
