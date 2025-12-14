#!/bin/bash

# Script to generate Kubernetes manifests for all services

# Service definitions: name, port, min_replicas, max_replicas
declare -A ECOMMERCE_SERVICES=(
  ["product-service"]="8001:2:10"
  ["inventory-service"]="8002:2:10"
  ["payment-service"]="8004:2:8"
  ["user-service"]="8005:2:8"
  ["cart-service"]="8006:2:8"
  ["notification-service"]="8007:2:6"
)

declare -A PROMOX_SERVICES=(
  ["campaign-service"]="9000:2:8"
  ["promotion-service"]="9001:3:12"
  ["flashsale-service"]="9002:2:10"
  ["coupon-service"]="9003:2:10"
  ["reward-service"]="9005:2:8"
  ["analytics-service"]="9006:2:8"
)

generate_manifest() {
  local service_name=$1
  local port=$2
  local min_replicas=$3
  local max_replicas=$4
  local output_dir=$5
  
  cat > "${output_dir}/${service_name}.yml" << EOF
# ============================================
# ${service_name^^} - Complete Manifest
# ============================================
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${service_name}
  namespace: ecommerce-platform
  labels:
    app: ${service_name}
spec:
  replicas: ${min_replicas}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: ${service_name}
  template:
    metadata:
      labels:
        app: ${service_name}
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "${port}"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: ecommerce-service-account
      containers:
        - name: ${service_name}
          image: ghcr.io/your-org/${service_name}:latest
          imagePullPolicy: Always
          ports:
            - containerPort: ${port}
          env:
            - name: SERVER_PORT
              value: "${port}"
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://\$(POSTGRES_HOST):\$(POSTGRES_PORT)/\$(POSTGRES_DB)"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: database-secrets
                  key: POSTGRES_USER
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: database-secrets
                  key: POSTGRES_PASSWORD
          envFrom:
            - configMapRef:
                name: database-config
            - configMapRef:
                name: monitoring-config
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: ${port}
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: ${port}
            initialDelaySeconds: 30
            periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: ${service_name}
  namespace: ecommerce-platform
spec:
  type: ClusterIP
  selector:
    app: ${service_name}
  ports:
    - port: ${port}
      targetPort: ${port}

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ${service_name}-hpa
  namespace: ecommerce-platform
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ${service_name}
  minReplicas: ${min_replicas}
  maxReplicas: ${max_replicas}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
EOF
  
  echo "✓ Generated: ${service_name}.yml"
}

echo "Generating Kubernetes manifests..."
echo ""

# Generate E-Commerce Services
for service in "${!ECOMMERCE_SERVICES[@]}"; do
  IFS=':' read -r port min max <<< "${ECOMMERCE_SERVICES[$service]}"
  generate_manifest "$service" "$port" "$min" "$max" "ecommerce-services"
done

# Generate PromoX Services
for service in "${!PROMOX_SERVICES[@]}"; do
  IFS=':' read -r port min max <<< "${PROMOX_SERVICES[$service]}"
  generate_manifest "$service" "$port" "$min" "$max" "promox-services"
done

echo ""
echo "✅ All manifests generated!"
echo "Total: 13 services (order-service already exists)"
