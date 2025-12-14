#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   STARTING MONITORING STACK${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if network exists
if ! docker network ls | grep -q ecommerce-network; then
    echo -e "${YELLOW}Creating ecommerce-network...${NC}"
    docker network create ecommerce-network
fi

# Start monitoring stack
echo -e "${YELLOW}Starting Prometheus...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d prometheus
sleep 5

echo -e "${YELLOW}Starting Grafana...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d grafana
sleep 5

echo -e "${YELLOW}Starting Alertmanager...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d alertmanager
sleep 3

echo -e "${YELLOW}Starting Zipkin...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d zipkin
sleep 3

echo -e "${YELLOW}Starting ELK Stack...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d elasticsearch
sleep 15
docker-compose -f docker-compose-monitoring.yml up -d logstash kibana
sleep 10

echo -e "${YELLOW}Starting Exporters...${NC}"
docker-compose -f docker-compose-monitoring.yml up -d postgres-exporter redis-exporter kafka-exporter
sleep 5

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   MONITORING STACK STARTED!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${GREEN}Access Points:${NC}"
echo -e "  Prometheus:    ${YELLOW}http://localhost:9090${NC}"
echo -e "  Grafana:       ${YELLOW}http://localhost:3000${NC} (admin/admin123)"
echo -e "  Alertmanager:  ${YELLOW}http://localhost:9093${NC}"
echo -e "  Zipkin:        ${YELLOW}http://localhost:9411${NC}"
echo -e "  Kibana:        ${YELLOW}http://localhost:5601${NC}"
echo ""
echo -e "${GREEN}Checking service health...${NC}"
sleep 5

# Health checks
echo -ne "Prometheus: "
if curl -s http://localhost:9090/-/healthy > /dev/null; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

echo -ne "Grafana: "
if curl -s http://localhost:3000/api/health > /dev/null; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

echo -ne "Zipkin: "
if curl -s http://localhost:9411/health > /dev/null; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

echo -ne "Elasticsearch: "
if curl -s http://localhost:9200/_cluster/health > /dev/null; then
    echo -e "${GREEN}✓ UP${NC}"
else
    echo -e "${RED}✗ DOWN${NC}"
fi

echo ""
echo -e "${GREEN}Monitoring stack is ready!${NC}"
echo -e "${YELLOW}Next: Update service configurations to enable metrics${NC}"
echo ""
