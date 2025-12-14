# 🚀 QUICK REFERENCE - E-COMMERCE PLATFORM

## ⚡ STARTUP (Choose One)

```bash
# Method 1: Script (Recommended)
./start-all.sh

# Method 2: Make
make up

# Method 3: Docker Compose
docker-compose up -d
```

**Total Time:** 2-3 minutes

---

## 🔍 CHECK STATUS

```bash
# Method 1: Script
./status.sh

# Method 2: Make
make status

# Method 3: Manual
docker-compose ps
curl http://localhost:8761  # Eureka
curl http://localhost:8080  # Gateway
```

---

## 🧪 TEST ORDER FLOW

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "baseAmount": 250.00,
    "promotionCode": "SUMMER2024",
    "couponCode": "WELCOME100"
  }'
```

**Expected:** 
- Base: $250
- After promo: $200 (-$50)
- After coupon: $100 (-$100)
- Points awarded: 100

---

## 📝 VIEW LOGS

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f order-service
docker-compose logs -f promotion-service
docker-compose logs -f api-gateway

# Search logs
docker-compose logs order-service | grep ERROR
```

---

## 🛑 STOP

```bash
# Method 1: Script
./stop-all.sh

# Method 2: Make
make down

# Method 3: Docker Compose
docker-compose down

# Stop + Delete Data
docker-compose down -v
```

---

## 🌐 ACCESS POINTS

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Order Service | http://localhost:8080/api/orders |
| Promotion Service | http://localhost:8080/api/promotions |
| Analytics | http://localhost:8080/api/analytics |

---

## 🔧 COMMON COMMANDS

```bash
# Restart service
docker-compose restart order-service

# Rebuild service
make rebuild-order-service

# Database shell
make db-shell

# Redis shell
make redis-shell

# Monitor resources
make monitor

# Clean everything
make clean
```

---

## 🐛 TROUBLESHOOTING

**Service won't start:**
```bash
docker-compose logs [service-name]
docker-compose restart [service-name]
```

**Can't connect to database:**
```bash
docker-compose restart postgres
sleep 10
docker-compose restart order-service
```

**Eureka not showing services:**
```bash
docker-compose restart service-discovery
sleep 20
docker-compose restart order-service
```

---

## 📊 15 MICROSERVICES

**Infrastructure (2):**
- Service Discovery (8761)
- API Gateway (8080)

**E-Commerce (7):**
- Product (8001)
- Inventory (8002)
- Order (8003) - **With PromoX Integration**
- Payment (8004)
- User (8005)
- Cart (8006)
- Notification (8007)

**PromoX Engine (6):**
- Campaign (9000)
- Promotion (9001)
- FlashSale (9002)
- Coupon (9003)
- Reward (9005)
- Analytics (9006)

---

## 💡 MAKE COMMANDS

```bash
make build       # Build all images
make up          # Start all services
make down        # Stop all services
make restart     # Restart all
make logs        # View logs
make logs-f      # Follow logs
make status      # Check status
make test        # Run tests
make clean       # Clean everything
make infra       # Start infrastructure only
make core        # Start Eureka + Gateway
make ecommerce   # Start e-commerce services
make promox      # Start PromoX services
```

---

## 📁 KEY FILES

```
ecommerce-platform/
├── docker-compose.yml          # All services
├── start-all.sh                # Sequential startup
├── stop-all.sh                 # Shutdown
├── Makefile                    # Commands
├── ARCHITECTURE.md             # System design
├── INTEGRATION_GUIDE.md        # Order flow
└── DOCKER_GUIDE.md             # Deployment
```

---

## 🎯 ORDER FLOW (7 STEPS)

1. Calculate base amount: $250
2. Apply promotion: -$50 → $200
3. Apply coupon: -$100 → $100
4. Process flash sale (if any)
5. Create order in DB
6. Award reward points: +100 (async)
7. Track analytics (async)

**Services Called:**
- Promotion Service
- Coupon Service
- Flash Sale Service (optional)
- Reward Service
- Analytics Service

---

## ✅ HEALTH CHECKS

```bash
# Eureka
curl http://localhost:8761/actuator/health

# Gateway
curl http://localhost:8080/actuator/health

# All services via Gateway
for port in 8001 8002 8003 8004 8005 8006 8007 9000 9001 9002 9003 9005 9006; do
  curl http://localhost:$port/actuator/health
done
```

---

**🚀 READY TO DEPLOY! ALL 15 SERVICES INTEGRATED!**
