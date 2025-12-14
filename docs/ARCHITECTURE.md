# 🏗️ E-COMMERCE PLATFORM - MICROSERVICES ARCHITECTURE

## 📊 SYSTEM OVERVIEW

**Total Services:** 15 microservices
- **E-Commerce Services:** 7 (Product, Inventory, Order, Payment, User, Cart, Notification)
- **PromoX Engine:** 6 (Campaign, Promotion, FlashSale, Coupon, Reward, Analytics)
- **Infrastructure:** 2 (API Gateway, Service Discovery)

---

## 🌐 ARCHITECTURE DIAGRAM

```
                         CLIENTS (Web/Mobile/API)
                                    |
                                    ↓
                    ┌───────────────────────────────┐
                    │      API GATEWAY (8080)       │
                    │  - Routing                    │
                    │  - Load Balancing             │
                    │  - Circuit Breaker            │
                    │  - Rate Limiting (Redis)      │
                    └───────────────┬───────────────┘
                                    |
                    ┌───────────────┴───────────────┐
                    ↓                               ↓
        ┌─────────────────────┐       ┌─────────────────────┐
        │  SERVICE DISCOVERY  │       │   INFRASTRUCTURE    │
        │   (Eureka - 8761)   │       │   - PostgreSQL      │
        │                     │       │   - Redis           │
        │  All services       │       │   - Kafka           │
        │  register here      │       └─────────────────────┘
        └─────────────────────┘
                    |
        ┌───────────┴──────────────────────────────────────┐
        │                                                   │
        ↓                                                   ↓
┌──────────────────┐                          ┌──────────────────────┐
│  E-COMMERCE      │                          │   PROMOX ENGINE      │
│  SERVICES        │                          │   SERVICES           │
│  (8001-8007)     │                          │   (9000-9006)        │
└──────────────────┘                          └──────────────────────┘
        │                                                   │
        ├─ Product Service (8001)                         ├─ Campaign Service (9000)
        │  └─> Search, Categories                         │  └─> State machine, scheduling
        │                                                  │
        ├─ Inventory Service (8002)                      ├─ Promotion Service (9001)
        │  └─> Stock management, locking                 │  └─> Rules engine, discount calc
        │                                                  │
        ├─ Order Service (8003) ◄──────┬────────────────►├─ FlashSale Service (9002)
        │  └─> Saga pattern, checkout   │                │  └─> Redis atomic ops, locks
        │                                │                │
        ├─ Payment Service (8004)        │               ├─ Coupon Service (9003)
        │  └─> Gateway, refunds          │               │  └─> Bulk gen, validation
        │                                │                │
        ├─ User Service (8005)           │               ├─ Reward Service (9005)
        │  └─> JWT, RBAC                 │               │  └─> 5-tier, achievements
        │                                │                │
        ├─ Cart Service (8006)           │               └─ Analytics Service (9006)
        │  └─> Merge, guest carts        │                  └─> Real-time, ROI, dashboard
        │                                │
        └─ Notification Service (8007)   │
           └─> Email, SMS, push          │
                                         │
                        ┌────────────────┘
                        │
                        ↓
              INTEGRATION LAYER
              - Order applies discounts (Promotion)
              - Order validates coupons (Coupon)
              - Order checks flashsale (FlashSale)
              - Order awards points (Reward)
              - All track usage (Analytics)
```

---

## 🔗 SERVICE COMMUNICATION

### **Synchronous (REST via Feign Client):**
```
Order Service ──GET──> Promotion Service (calculate discount)
Order Service ──POST─> Coupon Service (validate & redeem)
Order Service ──GET──> FlashSale Service (check availability)
Order Service ──POST─> Reward Service (award points)
Order Service ──POST─> Analytics Service (track usage)
```

### **Asynchronous (Kafka Events):**
```
Order Service ──publish──> ORDER_CREATED event
                           ├─> Payment Service (process payment)
                           ├─> Notification Service (send email)
                           ├─> Analytics Service (record metrics)
                           └─> Reward Service (award signup bonus)
```

---

## 🚀 PORT ALLOCATION

| Service | Port | Type |
|---------|------|------|
| **Infrastructure** |
| Service Discovery (Eureka) | 8761 | Infrastructure |
| API Gateway | 8080 | Infrastructure |
| **E-Commerce Services** |
| Product Service | 8001 | Business |
| Inventory Service | 8002 | Business |
| Order Service | 8003 | Business |
| Payment Service | 8004 | Business |
| User Service | 8005 | Business |
| Cart Service | 8006 | Business |
| Notification Service | 8007 | Business |
| **PromoX Engine** |
| Campaign Service | 9000 | Business |
| Promotion Service | 9001 | Business |
| FlashSale Service | 9002 | Business |
| Coupon Service | 9003 | Business |
| Reward Service | 9005 | Business |
| Analytics Service | 9006 | Business |
| **External** |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Cache |
| Kafka | 9092 | Message Broker |

---

## 📦 DATABASE ARCHITECTURE

### **E-Commerce Database (ecommerce_db):**
```sql
Tables:
- products
- categories
- inventory
- orders
- order_items
- payments
- users
- roles
- permissions
- carts
- cart_items
- notifications
```

### **PromoX Database (promox_db):**
```sql
Tables:
- campaigns
- promotions
- promotion_rules
- promotion_usage_log
- flash_sales
- flash_sale_purchases
- coupons
- coupon_redemptions
- reward_accounts
- point_transactions
- achievements
- user_achievements
- campaign_analytics
- promotion_analytics
```

---

## 🔧 TECHNOLOGY STACK

### **Backend:**
- **Framework:** Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **Language:** Java 17
- **Build:** Maven
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Circuit Breaker:** Resilience4j

### **Database:**
- **Relational:** PostgreSQL 15
- **Cache:** Redis 7
- **Message Broker:** Apache Kafka

### **Security:**
- **Authentication:** JWT
- **Authorization:** Role-Based Access Control (RBAC)

### **Monitoring:**
- **Health Checks:** Spring Boot Actuator
- **Metrics:** Micrometer
- **Logging:** SLF4J + Logback

---

## 🛠️ SETUP & RUN

### **1. Start Infrastructure:**
```bash
# Start Eureka Server
cd service-discovery
mvn spring-boot:run

# Start API Gateway
cd api-gateway
mvn spring-boot:run
```

### **2. Start E-Commerce Services:**
```bash
# Each service in separate terminal
cd product-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd cart-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### **3. Start PromoX Services:**
```bash
cd promox-campaign-service && mvn spring-boot:run
cd promox-promotion-service && mvn spring-boot:run
cd promox-flashsale-service && mvn spring-boot:run
cd promox-coupon-service && mvn spring-boot:run
cd promox-reward-service && mvn spring-boot:run
cd promox-analytics-service && mvn spring-boot:run
```

### **4. Access:**
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8080
- **All APIs via Gateway:** http://localhost:8080/api/*

---

## 🧪 API EXAMPLES

### **Through API Gateway:**

```bash
# Get Products
GET http://localhost:8080/api/products

# Create Order with Promotion
POST http://localhost:8080/api/orders
{
  "items": [...],
  "promotionCode": "SUMMER2024",
  "couponCode": "WELCOME100"
}

# Check Reward Balance
GET http://localhost:8080/api/rewards/account/1

# Get Analytics Dashboard
GET http://localhost:8080/api/analytics/dashboard/summary
```

---

## 🚀 DEPLOYMENT

### **Docker Compose (Coming Next):**
```bash
docker-compose up -d
```

This will start:
- All 13 microservices
- PostgreSQL
- Redis
- Kafka
- Eureka Server
- API Gateway

---

**🏗️ ARCHITECTURE COMPLETE! READY FOR INTEGRATION!** 🎉
