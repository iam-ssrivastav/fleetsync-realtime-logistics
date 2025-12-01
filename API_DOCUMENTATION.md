# FleetSync REST API Documentation

## Overview
FleetSync provides real-time REST APIs to monitor fleet status, Kafka metrics, and system health.

## Base URL
```
http://localhost:8080/api
```

---

## Fleet APIs

### 1. Get Fleet Statistics
**Endpoint:** `GET /api/fleet/stats`

**Description:** Returns real-time aggregated statistics for the entire fleet.

**Response:**
```json
{
    "activeTrucks": 5,
    "averageSpeed": 72.3,
    "averageEngineTemp": 85.4,
    "averageFuelLevel": 66.2,
    "timestamp": 1764615599362
}
```

**Example:**
```bash
curl http://localhost:8080/api/fleet/stats
```

---

### 2. Get All Trucks
**Endpoint:** `GET /api/fleet/trucks`

**Description:** Returns current telemetry data for all active trucks.

**Response:**
```json
{
    "trucks": [
        {
            "truckId": "TRUCK-001",
            "latitude": 40.7589,
            "longitude": -73.9851,
            "speed": 65.3,
            "engineTemp": 82.1,
            "fuelLevel": 75.0
        },
        ...
    ],
    "count": 5,
    "timestamp": 1764615599362
}
```

**Example:**
```bash
curl http://localhost:8080/api/fleet/trucks
```

---

### 3. Get Recent Alerts
**Endpoint:** `GET /api/fleet/alerts`

**Description:** Returns the 50 most recent alerts.

**Response:**
```json
{
    "alerts": [
        "TRUCK-003: SPEEDING",
        "TRUCK-001: LOW FUEL",
        "TRUCK-005: OVERHEATING"
    ],
    "count": 3
}
```

**Example:**
```bash
curl http://localhost:8080/api/fleet/alerts
```

---

## Kafka Metrics APIs

### 4. Get Kafka Consumer Metrics
**Endpoint:** `GET /api/metrics/kafka/consumer`

**Description:** Returns detailed metrics about the Kafka consumer group.

**Response:**
```json
{
    "groupId": "fleetsync-dashboard",
    "state": "Stable",
    "members": 1,
    "partitions": [
        {
            "topic": "fleet-telemetry",
            "partition": 0,
            "currentOffset": 695,
            "metadata": ""
        }
    ],
    "memberDetails": [
        {
            "memberId": "consumer-fleetsync-dashboard-1-7c564822...",
            "clientId": "consumer-fleetsync-dashboard-1",
            "host": "/192.168.65.1"
        }
    ]
}
```

**Key Metrics:**
- `state`: Consumer group state (Stable, Rebalancing, Dead)
- `currentOffset`: Number of messages processed
- `members`: Number of active consumers

**Example:**
```bash
curl http://localhost:8080/api/metrics/kafka/consumer
```

---

### 5. Get Kafka Topics
**Endpoint:** `GET /api/metrics/kafka/topics`

**Description:** Lists all Kafka topics.

**Response:**
```json
{
    "topics": ["fleet-telemetry"],
    "count": 1
}
```

**Example:**
```bash
curl http://localhost:8080/api/metrics/kafka/topics
```

---

## Health Check APIs

### 6. Check Kafka Health
**Endpoint:** `GET /api/health/kafka`

**Description:** Verifies Kafka connectivity.

**Response (UP):**
```json
{
    "status": "UP",
    "clusterId": "sxCEtR-MScC7w8H2CvkJmg",
    "nodes": 1,
    "bootstrapServers": ["localhost:9093"]
}
```

**Response (DOWN):**
```json
{
    "status": "DOWN",
    "error": "Connection refused"
}
```

**Example:**
```bash
curl http://localhost:8080/api/health/kafka
```

---

### 7. Check System Health
**Endpoint:** `GET /api/health/system`

**Description:** Returns overall system status.

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

**Example:**
```bash
curl http://localhost:8080/api/health/system
```

---

## System Metrics APIs

### 8. Get Real-Time System Metrics
**Endpoint:** `GET /api/metrics/realtime`

**Description:** Returns JVM and system metrics.

**Response:**
```json
{
    "system": {
        "totalMemoryMB": 68,
        "freeMemoryMB": 21,
        "usedMemoryMB": 46,
        "processors": 10,
        "uptime": 30476
    },
    "application": "FleetSync",
    "timestamp": 1764615598764
}
```

**Example:**
```bash
curl http://localhost:8080/api/metrics/realtime
```

---

## Monitoring Dashboard Script

Create a simple monitoring script:

```bash
#!/bin/bash
# monitor.sh

while true; do
    clear
    echo "=== FleetSync Real-Time Monitor ==="
    echo ""
    
    echo "Fleet Stats:"
    curl -s http://localhost:8080/api/fleet/stats | python3 -m json.tool
    echo ""
    
    echo "Kafka Consumer:"
    curl -s http://localhost:8080/api/metrics/kafka/consumer | python3 -m json.tool | grep -E '(state|currentOffset)'
    echo ""
    
    echo "System:"
    curl -s http://localhost:8080/api/metrics/realtime | python3 -m json.tool | grep -E '(usedMemoryMB|uptime)'
    
    sleep 5
done
```

**Usage:**
```bash
chmod +x monitor.sh
./monitor.sh
```

---

## Integration Examples

### Python
```python
import requests

# Get fleet stats
response = requests.get('http://localhost:8080/api/fleet/stats')
stats = response.json()
print(f"Average Speed: {stats['averageSpeed']} mph")
```

### JavaScript (Node.js)
```javascript
const axios = require('axios');

async function getFleetStats() {
    const response = await axios.get('http://localhost:8080/api/fleet/stats');
    console.log(`Active Trucks: ${response.data.activeTrucks}`);
}
```

### cURL with Watch
```bash
# Monitor fleet stats every 2 seconds
watch -n 2 'curl -s http://localhost:8080/api/fleet/stats | python3 -m json.tool'
```
