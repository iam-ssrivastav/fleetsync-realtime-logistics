# FleetSync REST API Documentation

## Overview
FleetSync provides real-time REST APIs to monitor fleet status, Kafka metrics, and system health.

## Base URL
```
http://localhost:8080/api
```

---

## 📜 Historical Data APIs (New)

### 1. Get Historical Telemetry
**Endpoint:** `GET /api/history/telemetry`

**Parameters:**
- `from` (optional): Start timestamp (Unix ms)
- `to` (optional): End timestamp (Unix ms)
- `limit` (optional): Max records (default 100)

**Response:**
```json
{
    "data": [
        {
            "truckId": "TRUCK-001",
            "speed": 65.3,
            "timestamp": 1764615599362
        }
    ],
    "count": 100,
    "totalRecords": 5420
}
```

---

### 2. Get Truck History
**Endpoint:** `GET /api/history/truck/{truckId}`

**Description:** Get historical path for a specific truck.

**Example:**
```bash
curl http://localhost:8080/api/history/truck/TRUCK-001
```

---

## 📊 Fleet APIs

### 3. Get Fleet Statistics
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

---

### 4. Get All Trucks
**Endpoint:** `GET /api/fleet/trucks`

**Description:** Returns current telemetry data for all active trucks.

---

### 5. Get Recent Alerts
**Endpoint:** `GET /api/fleet/alerts`

**Description:** Returns the 50 most recent alerts.

---

## 🔧 Kafka Metrics APIs

### 6. Get Kafka Consumer Metrics
**Endpoint:** `GET /api/metrics/kafka/consumer`

**Description:** Returns detailed metrics about the Kafka consumer group.

---

### 7. Get Kafka Topics
**Endpoint:** `GET /api/metrics/kafka/topics`

**Description:** Lists all Kafka topics.

---

## 💚 Health Check APIs

### 8. Check Kafka Health
**Endpoint:** `GET /api/health/kafka`

**Description:** Verifies Kafka connectivity.

---

### 9. Check System Health
**Endpoint:** `GET /api/health/system`

**Description:** Returns overall system status.

---

## 🖥️ System Metrics APIs

### 10. Get Real-Time System Metrics
**Endpoint:** `GET /api/metrics/realtime`

**Description:** Returns JVM and system metrics.
