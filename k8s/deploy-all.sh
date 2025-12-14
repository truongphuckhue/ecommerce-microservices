#!/bin/bash

# Complete Kubernetes Deployment Script
# Deploys all 15 microservices + infrastructure

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   KUBERNETES DEPLOYMENT${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl is not installed${NC}"
    exit 1
fi

# Check cluster connection
echo -e "${YELLOW}Checking Kubernetes cluster...${NC}"
if ! kubectl cluster-info &> /dev/null; then
    echo -e "${RED}❌ Cannot connect to Kubernetes cluster${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Connected to cluster${NC}"
echo ""

# Create namespaces
echo -e "${YELLOW}📦 Creating namespaces...${NC}"
kubectl apply -f 00-namespaces.yml
echo ""

# Create RBAC
echo -e "${YELLOW}🔐 Setting up RBAC...${NC}"
kubectl apply -f rbac.yml
echo ""

# Create ConfigMaps and Secrets
echo -e "${YELLOW}⚙️  Creating ConfigMaps...${NC}"
kubectl apply -f configmaps/
echo ""

echo -e "${YELLOW}🔑 Creating Secrets...${NC}"
kubectl apply -f secrets/
echo ""

# Deploy Infrastructure
echo -e "${YELLOW}🏗️  Deploying Infrastructure...${NC}"
kubectl apply -f infrastructure/databases.yml
echo -ne "  Waiting for databases..."
sleep 30
echo -e " ${GREEN}✓${NC}"

kubectl apply -f infrastructure/service-discovery.yml
echo -ne "  Waiting for Eureka..."
sleep 20
echo -e " ${GREEN}✓${NC}"

kubectl apply -f infrastructure/api-gateway.yml
echo -ne "  Waiting for API Gateway..."
sleep 15
echo -e " ${GREEN}✓${NC}"
echo ""

# Deploy E-Commerce Services
echo -e "${YELLOW}🛒 Deploying E-Commerce Services...${NC}"
for service in product inventory order payment user cart notification; do
    echo -ne "  Deploying ${service}-service..."
    kubectl apply -f ecommerce-services/${service}-service.yml > /dev/null
    echo -e " ${GREEN}✓${NC}"
done
echo ""

# Deploy PromoX Services
echo -e "${YELLOW}🎁 Deploying PromoX Services...${NC}"
for service in campaign promotion flashsale coupon reward analytics; do
    echo -ne "  Deploying ${service}-service..."
    kubectl apply -f promox-services/${service}-service.yml > /dev/null
    echo -e " ${GREEN}✓${NC}"
done
echo ""

# Wait for all pods to be ready
echo -e "${YELLOW}⏳ Waiting for all pods to be ready...${NC}"
kubectl wait --for=condition=ready pod \
    -l tier=ecommerce \
    -n ecommerce-platform \
    --timeout=300s

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   ✅ DEPLOYMENT COMPLETE!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Show status
echo -e "${BLUE}Service Status:${NC}"
kubectl get pods -n ecommerce-platform

echo ""
echo -e "${BLUE}Services:${NC}"
kubectl get svc -n ecommerce-platform

echo ""
echo -e "${BLUE}HorizontalPodAutoscalers:${NC}"
kubectl get hpa -n ecommerce-platform

echo ""
echo -e "${GREEN}🎉 All 15 microservices deployed successfully!${NC}"
echo ""
echo -e "${YELLOW}Access API Gateway:${NC}"
echo "  kubectl port-forward svc/api-gateway 8080:80 -n ecommerce-platform"
echo ""
echo -e "${YELLOW}View logs:${NC}"
echo "  kubectl logs -f deployment/order-service -n ecommerce-platform"
echo ""
