# Kafka Health Check Guide

## Quick Health Check APIs

I've added REST endpoints to verify Kafka connectivity:

### 1. Check Kafka Status
```bash
curl http://localhost:8080/api/health/kafka
```

**Response (Kafka UP):**
```json
{
    "status": "UP",
    "clusterId": "sxCEtR-MScC7w8H2CvkJmg",
    "nodes": 1,
    "bootstrapServers": ["localhost:9093"]
}
```

**Response (Kafka DOWN):**
```json
{
    "status": "DOWN",
    "error": "Connection refused"
}
```

### 2. Check Overall System Health
```bash
curl http://localhost:8080/api/health/system
```

**Response:**
```json
{
    "application": "FleetSync",
    "status": "UP",
    "components": {
        "mqtt": "UP",
        "websocket": "UP",
        "kafka": "Check /api/health/kafka"
    }
}
```

## Manual Kafka Verification

### 1. Check Docker Containers
```bash
docker ps | grep kafka
```

**Expected Output:**
```
CONTAINER ID   IMAGE                             STATUS
abc123         confluentinc/cp-kafka:7.4.0       Up 5 minutes
def456         confluentinc/cp-zookeeper:7.4.0   Up 5 minutes
```

### 2. Check Kafka Topics
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9093
```

**Expected Output:**
```
fleet-telemetry
```

### 3. Consume Messages Directly
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic fleet-telemetry \
  --from-beginning \
  --max-messages 5
```

**Expected Output:**
```json
{"truckId":"TRUCK-001","latitude":40.7589,"longitude":-73.9851,"speed":65.3,...}
{"truckId":"TRUCK-002","latitude":40.7612,"longitude":-73.9776,"speed":72.1,...}
```

### 4. Check Consumer Group
```bash
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9093 \
  --describe \
  --group fleetsync-dashboard
```

**Expected Output:**
```
GROUP               TOPIC           PARTITION  CURRENT-OFFSET  LAG
fleetsync-dashboard fleet-telemetry 0          325             0
```

- **LAG = 0**: Consumer is caught up ✅
- **LAG > 0**: Consumer is behind (backlog)

## Application Logs

Check Spring Boot logs for Kafka activity:

```bash
# Look for these log lines
grep -i kafka logs/spring.log
```

**Key Log Messages:**
```
✅ Kafka consumer connected: fleet-telemetry-0
✅ Successfully synced group in generation
✅ partitions assigned: [fleet-telemetry-0]
```

## Troubleshooting

### Problem: "Connection refused" on health check

**Solution:**
```bash
# Restart Kafka
docker-compose down
docker-compose up -d

# Wait 15 seconds for Kafka to be ready
sleep 15
```

### Problem: LAG is increasing

**Cause:** Consumer is slower than producer

**Solution:**
- Check application logs for errors
- Verify WebSocket connections are active
- Consider adding more consumer instances

### Problem: No messages in topic

**Cause:** MQTT simulator not running or TelemetryService not producing

**Solution:**
```bash
# Check application logs
tail -f logs/spring.log | grep -i "kafka\|mqtt"
```

## Production Monitoring

For production, integrate with:
- **Prometheus**: Kafka metrics exporter
- **Grafana**: Dashboard for Kafka lag, throughput
- **Spring Boot Actuator**: `/actuator/health` endpoint
