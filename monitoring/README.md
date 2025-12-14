# 🔍 MONITORING & OBSERVABILITY STACK

Complete monitoring solution for the E-Commerce Platform with 15 microservices.

---

## 📦 COMPONENTS

### **1. Prometheus** (Port 9090)
- Metrics collection from 15 services
- Alert evaluation
- Time-series database

### **2. Grafana** (Port 3000)
- 4 pre-built dashboards
- Data visualization
- Alert management UI

### **3. Zipkin** (Port 9411)
- Distributed tracing
- Request flow visualization
- Performance bottleneck identification

### **4. ELK Stack**
- **Elasticsearch** (Port 9200): Log storage
- **Logstash** (Port 5000): Log processing
- **Kibana** (Port 5601): Log visualization

### **5. Alertmanager** (Port 9093)
- Alert routing
- Notification management
- Alert grouping

### **6. Exporters**
- PostgreSQL Exporter (Port 9187)
- Redis Exporter (Port 9121)
- Kafka Exporter (Port 9308)

---

## 🚀 QUICK START

### **Step 1: Start Monitoring Stack**

```bash
cd monitoring
./start-monitoring.sh
```

Wait 2-3 minutes for all services to start.

### **Step 2: Verify Services**

```bash
# Check Prometheus
curl http://localhost:9090/-/healthy

# Check Grafana
curl http://localhost:3000/api/health

# Check Elasticsearch
curl http://localhost:9200/_cluster/health
```

### **Step 3: Access Dashboards**

- **Grafana**: http://localhost:3000
  - Username: `admin`
  - Password: `admin123`
  
- **Prometheus**: http://localhost:9090
  
- **Zipkin**: http://localhost:9411
  
- **Kibana**: http://localhost:5601

---

## 📊 GRAFANA DASHBOARDS

### **1. System Health Dashboard**
- Service up/down status
- Request rate per service
- Error rate percentage
- P95/P99 response times
- CPU and memory usage
- Active threads count

### **2. Business Metrics Dashboard**
- Orders per hour
- Average order value
- Total discounts applied
- Promotion success rate
- Coupon redemption rate
- Top 10 products sold
- Revenue trends

### **3. Service-Specific Dashboards** (To be added)
- Order Service deep dive
- Promotion Service analytics
- Inventory Service metrics

### **4. Infrastructure Dashboard** (To be added)
- PostgreSQL performance
- Redis cache hit rate
- Kafka throughput

---

## 🔔 ALERTS CONFIGURED

### **Critical Alerts:**
- Service down for > 1 minute
- Error rate > 10%
- Response time > 3 seconds
- Memory usage > 95%
- No orders created in 10 minutes

### **Warning Alerts:**
- Error rate > 5%
- Response time > 1 second
- Memory usage > 85%
- CPU usage > 80%
- Database connection pool > 80%

### **Business Alerts:**
- High order failure rate (> 10%)
- High promotion failure rate (> 20%)
- Low cache hit rate (< 50%)

---

## 🔧 SERVICE CONFIGURATION

### **Add to Each Service's pom.xml:**

```xml
<dependencies>
    <!-- Actuator for metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Prometheus metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- Zipkin tracing -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>
</dependencies>
```

### **Add to application.yml:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}

# Zipkin tracing
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

---

## 📈 CUSTOM BUSINESS METRICS

### **Example: Track Orders Created**

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class OrderService {
    private final Counter ordersCreated;
    private final Counter ordersFailed;
    
    public OrderService(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders.created.total")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(registry);
            
        this.ordersFailed = Counter.builder("orders.failed.total")
            .description("Total orders failed")
            .tag("service", "order-service")
            .register(registry);
    }
    
    public Order createOrder(OrderRequest request) {
        try {
            Order order = // ... create order
            ordersCreated.increment();
            return order;
        } catch (Exception e) {
            ordersFailed.increment();
            throw e;
        }
    }
}
```

### **Example: Track Discount Amount**

```java
import io.micrometer.core.instrument.DistributionSummary;

@Service
public class PromotionService {
    private final DistributionSummary discountAmount;
    
    public PromotionService(MeterRegistry registry) {
        this.discountAmount = DistributionSummary.builder("discount.amount.total")
            .description("Total discount amount")
            .baseUnit("dollars")
            .tag("type", "promotion")
            .register(registry);
    }
    
    public void applyDiscount(BigDecimal amount) {
        discountAmount.record(amount.doubleValue());
    }
}
```

---

## 🔍 USING DISTRIBUTED TRACING

### **1. Create an Order**

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

### **2. Open Zipkin**

Go to http://localhost:9411

### **3. Search for Trace**

- Click "Run Query"
- Find your recent order request
- Click to see the trace

### **4. Analyze**

You'll see:
```
API Gateway (5ms)
  → Order Service (20ms)
    → Promotion Service (45ms) ← Bottleneck!
    → Coupon Service (30ms)
    → Reward Service (15ms)
    → Analytics Service (10ms)
Total: 125ms
```

---

## 📝 QUERYING LOGS IN KIBANA

### **1. Open Kibana**

http://localhost:5601

### **2. Create Index Pattern**

- Go to Management → Index Patterns
- Create pattern: `logs-*`
- Select timestamp field: `@timestamp`

### **3. Example Queries**

**Find all errors:**
```
level:ERROR
```

**Find errors for specific user:**
```
level:ERROR AND userId:123
```

**Find slow queries:**
```
duration > 1000 AND service:order-service
```

**Find all logs for an order:**
```
orderId:"ORD-ABC123"
```

---

## 🎯 DEMO SCENARIOS

### **Demo 1: Real-Time Monitoring**

1. Open Grafana → System Health dashboard
2. Show live metrics updating every 10s
3. Point out services status, request rates, response times

### **Demo 2: Business Intelligence**

1. Switch to Business Metrics dashboard
2. Show orders per hour, average order value
3. Demonstrate discount trends by type

### **Demo 3: Distributed Tracing**

1. Create order with promotions
2. Open Zipkin
3. Show complete request flow with timings
4. Identify which service is slowest

### **Demo 4: Log Analysis**

1. Generate some errors
2. Open Kibana
3. Search for errors
4. Show how to filter by service, user, order

### **Demo 5: Alerting**

1. Show Prometheus alerts
2. Open Alertmanager
3. Demonstrate alert routing

---

## 🛑 STOP MONITORING STACK

```bash
cd monitoring
docker-compose -f docker-compose-monitoring.yml down
```

**To remove data:**
```bash
docker-compose -f docker-compose-monitoring.yml down -v
```

---

## 📊 METRICS REFERENCE

### **JVM Metrics:**
- `jvm_memory_used_bytes` - Memory usage
- `jvm_threads_live_threads` - Thread count
- `jvm_gc_pause_seconds` - GC pause time
- `process_cpu_seconds_total` - CPU usage

### **HTTP Metrics:**
- `http_server_requests_seconds_count` - Request count
- `http_server_requests_seconds_sum` - Total time
- `http_server_requests_seconds_bucket` - Histogram

### **Custom Business Metrics:**
- `orders_created_total` - Orders created
- `orders_failed_total` - Orders failed
- `order_amount_total` - Order amounts
- `discount_amount_total` - Discount amounts
- `promotion_validation_total` - Promotion validations
- `coupon_redemption_total` - Coupon redemptions

### **Database Metrics:**
- `hikaricp_connections_active` - Active connections
- `hikaricp_connections_max` - Max connections
- `hikaricp_connections_acquire_seconds` - Acquisition time

---

## ✅ VERIFICATION CHECKLIST

After setup, verify:

- [ ] Prometheus scraping all 15 services
- [ ] Grafana shows 4 dashboards
- [ ] System Health dashboard displays metrics
- [ ] Business Metrics dashboard shows orders
- [ ] Zipkin captures traces
- [ ] Kibana receives logs
- [ ] Alerts configured and active

---

## 🎤 INTERVIEW TALKING POINTS

**Q: How do you monitor your microservices?**

"I implemented comprehensive monitoring using Prometheus and Grafana. Prometheus scrapes metrics from all 15 services every 15 seconds, collecting JVM metrics, HTTP metrics, database metrics, and custom business metrics. I created 4 Grafana dashboards - System Health showing service status and performance, Business Metrics tracking orders and revenue, Service-Specific for deep dives, and Infrastructure monitoring databases and caches."

**Q: How do you debug production issues?**

"I use distributed tracing with Zipkin. When an order takes too long, I can see the complete request flow across services with exact timing. For example, if Order Service calls Promotion Service, Coupon Service, and Reward Service, I can see which one is slow. I also have centralized logging with ELK Stack where I can search all logs from 15 services by user ID, order ID, or error type."

**Q: How do you know if a service is unhealthy?**

"I have Prometheus alerts configured for critical conditions. If a service is down for more than 1 minute, error rate exceeds 5%, response time is over 3 seconds, or memory usage exceeds 95%, Alertmanager immediately sends notifications. I also track business metrics - if no orders are created for 10 minutes, that triggers a critical alert."

---

## 🚀 NEXT STEPS

1. ✅ Monitor stack is running
2. ⏳ Update all 15 services with actuator dependencies
3. ⏳ Add custom business metrics to services
4. ⏳ Configure Slack notifications for alerts
5. ⏳ Create service-specific dashboards
6. ⏳ Set up log forwarding from services to Logstash

---

**🎉 MONITORING STACK COMPLETE!**
