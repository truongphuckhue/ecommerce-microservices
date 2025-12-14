# 📊 Performance Benchmarks & Load Test Results

## Test Environment

**Hardware:**
- CPU: 8 cores
- RAM: 16 GB
- Disk: SSD

**Platform Configuration:**
- Microservices: 15 services running
- Database: PostgreSQL
- Cache: Redis
- Message Queue: Kafka

## Test Scenarios

### 1. Authentication Load Test

**Configuration:**
- Virtual Users: 50
- Ramp-up Time: 10 seconds
- Iterations: 10 per user
- Total Requests: 500

**Expected Results:**
| Metric | Target | Threshold |
|--------|--------|-----------|
| Average Response Time | < 200ms | < 500ms |
| 95th Percentile | < 500ms | < 1000ms |
| Error Rate | < 1% | < 5% |
| Throughput | > 100 req/s | > 50 req/s |

### 2. Product Browsing Load Test

**Configuration:**
- Virtual Users: 100
- Ramp-up Time: 20 seconds
- Iterations: 20 per user
- Total Requests: 2000

**Expected Results:**
| Metric | Target | Threshold |
|--------|--------|-----------|
| Average Response Time | < 150ms | < 400ms |
| 95th Percentile | < 400ms | < 800ms |
| Error Rate | < 0.5% | < 3% |
| Throughput | > 200 req/s | > 100 req/s |

### 3. Order Creation (Saga Pattern)

**Configuration:**
- Virtual Users: 20
- Ramp-up Time: 10 seconds
- Iterations: 5 per user
- Total Requests: 100

**Expected Results:**
| Metric | Target | Threshold |
|--------|--------|-----------|
| Average Response Time | < 2000ms | < 5000ms |
| 95th Percentile | < 5000ms | < 10000ms |
| Error Rate | < 2% | < 10% |
| Saga Success Rate | > 95% | > 85% |

**Note:** Order creation involves distributed transaction across 4 services:
1. Order Service
2. Inventory Service (via Kafka)
3. Payment Service (via Kafka)
4. Notification Service (via Kafka)

## Benchmarking Goals

### Response Time Targets

| Service | Endpoint | P50 | P95 | P99 |
|---------|----------|-----|-----|-----|
| Product Service | GET /api/products | 50ms | 200ms | 500ms |
| Product Service | GET /api/products/{id} | 30ms | 100ms | 300ms |
| Product Service | POST /api/products | 100ms | 300ms | 700ms |
| Inventory Service | Reserve Stock | 150ms | 400ms | 800ms |
| Order Service | Create Order | 500ms | 2000ms | 5000ms |
| Payment Service | Process Payment | 300ms | 1000ms | 3000ms |
| User Service | Login | 100ms | 300ms | 600ms |
| User Service | Register | 200ms | 500ms | 1000ms |

### Throughput Targets

| Service | Target RPS | Max RPS |
|---------|-----------|---------|
| Product Service | 500 | 1000 |
| Order Service | 50 | 100 |
| User Service | 200 | 400 |
| API Gateway | 1000 | 2000 |

### Resource Utilization Targets

| Resource | Target | Threshold |
|----------|--------|-----------|
| CPU Usage | < 70% | < 85% |
| Memory Usage | < 75% | < 90% |
| DB Connections | < 50% | < 80% |
| Kafka Lag | < 100 msgs | < 1000 msgs |

## Running the Tests

### Quick Start

```bash
cd load-tests
./run-load-test.sh
```

### Custom Configuration

```bash
# Run with custom thread count
jmeter -n -t jmeter/ecommerce-load-test.jmx \
  -Jusers=100 \
  -Jrampup=30 \
  -Jloops=50 \
  -l results/custom-test.jtl

# Run specific thread group
jmeter -n -t jmeter/ecommerce-load-test.jmx \
  -JthreadGroupName="01 - Authentication Load" \
  -l results/auth-test.jtl
```

### Viewing Results

```bash
# Open HTML report
open results/html-report/index.html

# Generate report from existing JTL
jmeter -g results/results.jtl -o results/custom-report
```

## Monitoring During Tests

### Grafana Dashboards

Access: http://localhost:3000

**Key Metrics to Watch:**
- Request rate per service
- Response times (P50, P95, P99)
- Error rates
- JVM heap usage
- Database connection pool

### Prometheus Queries

```promql
# Average response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# CPU usage
system_cpu_usage

# Memory usage
jvm_memory_used_bytes{area="heap"}
```

## Stress Testing Scenarios

### Scenario 1: Spike Test
- Ramp up to 500 users in 10 seconds
- Hold for 5 minutes
- Ramp down

**Goal:** Test auto-scaling and recovery

### Scenario 2: Soak Test
- 100 users constant load
- Duration: 2 hours
- **Goal:** Find memory leaks

### Scenario 3: Breaking Point
- Gradually increase load until failure
- Find maximum capacity
- **Goal:** Determine system limits

## Results Analysis

### Success Criteria

✅ **PASSED** if:
- All target response times met
- Error rate < 5%
- No memory leaks detected
- Auto-scaling triggered correctly
- Saga compensation works correctly

❌ **FAILED** if:
- Response times exceed thresholds
- Error rate > 10%
- Services crash
- Database deadlocks occur
- Kafka lag > 1000 messages

### Common Issues

1. **High Response Times**
   - Check database indexes
   - Review N+1 queries
   - Verify connection pooling

2. **High Error Rates**
   - Check service logs
   - Verify Kafka connectivity
   - Check database timeouts

3. **Memory Leaks**
   - Review heap dumps
   - Check for connection leaks
   - Verify cache eviction

## CI/CD Integration

Add to GitHub Actions:

```yaml
- name: Run Performance Tests
  run: |
    cd load-tests
    ./run-load-test.sh
    
- name: Upload Results
  uses: actions/upload-artifact@v3
  with:
    name: load-test-results
    path: load-tests/results/
```

## Next Steps

1. **Optimize Hot Paths**
   - Cache frequently accessed data
   - Add database indexes
   - Optimize queries

2. **Implement Circuit Breakers**
   - Prevent cascade failures
   - Fast fail on errors

3. **Add Rate Limiting**
   - Per-user limits
   - Per-IP limits

4. **Tune Database**
   - Connection pool size
   - Query timeout
   - Index optimization

## References

- JMeter Best Practices: https://jmeter.apache.org/usermanual/best-practices.html
- Performance Testing Guide: https://martinfowler.com/articles/performanceT esting.html
- Microservices Performance: https://microservices.io/patterns/observability/performance-metrics.html
