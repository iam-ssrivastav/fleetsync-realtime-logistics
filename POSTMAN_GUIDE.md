# FleetSync Postman Collection

## Quick Start

### 1. Import into Postman

**Option A: Import File**
1. Open Postman
2. Click **Import** button (top left)
3. Select **File** tab
4. Choose `FleetSync_API_Collection.postman_collection.json`
5. Click **Import**

**Option B: Import via URL** (if hosted on GitHub)
1. Open Postman
2. Click **Import** button
3. Select **Link** tab
4. Paste the raw GitHub URL
5. Click **Continue** → **Import**

### 2. Verify Environment Variable

The collection uses a variable `{{base_url}}` which is set to `http://localhost:8080` by default.

**To change the base URL:**
1. Click on the collection name in Postman
2. Go to **Variables** tab
3. Update `base_url` value (e.g., `http://production-server:8080`)

### 3. Start Using the APIs

The collection is organized into 4 folders:

#### 📊 Fleet APIs
- **Get Fleet Statistics** - Aggregated fleet metrics
- **Get All Trucks** - Individual truck telemetry
- **Get Recent Alerts** - Alert history

#### 🔧 Kafka Metrics
- **Get Kafka Consumer Metrics** - Consumer group status
- **Get Kafka Topics** - List of topics

#### 💚 Health Checks
- **Check Kafka Health** - Kafka connectivity
- **Check System Health** - Overall system status

#### 🖥️ System Metrics
- **Get Real-Time System Metrics** - JVM and memory stats

## Running Requests

### Single Request
1. Select any request from the collection
2. Click **Send**
3. View the response in the bottom panel

### Run All Requests (Collection Runner)
1. Click on the collection name
2. Click **Run** button
3. Select requests to run
4. Click **Run FleetSync - Real-Time Logistics API**

## Example Responses

### Fleet Statistics
```json
{
    "activeTrucks": 5,
    "averageSpeed": 72.3,
    "averageEngineTemp": 85.4,
    "averageFuelLevel": 66.2,
    "timestamp": 1764615599362
}
```

### Kafka Consumer Metrics
```json
{
    "groupId": "fleetsync-dashboard",
    "state": "Stable",
    "members": 1,
    "partitions": [
        {
            "topic": "fleet-telemetry",
            "partition": 0,
            "currentOffset": 695
        }
    ]
}
```

## Testing Scenarios

### Scenario 1: Monitor Fleet Health
1. Run **Get Fleet Statistics**
2. Run **Get Recent Alerts**
3. Check if `averageSpeed` is within normal range
4. Verify no critical alerts

### Scenario 2: Verify Kafka Pipeline
1. Run **Check Kafka Health** → Should return `"status": "UP"`
2. Run **Get Kafka Consumer Metrics** → Check `"state": "Stable"`
3. Run **Get Kafka Topics** → Verify `fleet-telemetry` exists

### Scenario 3: System Performance Check
1. Run **Get Real-Time System Metrics**
2. Check `usedMemoryMB` is not too high
3. Verify `uptime` is increasing

## Automation with Newman (CLI)

You can run this collection from the command line using Newman:

```bash
# Install Newman
npm install -g newman

# Run the collection
newman run FleetSync_API_Collection.postman_collection.json

# Run with environment file
newman run FleetSync_API_Collection.postman_collection.json \
  --env-var "base_url=http://production:8080"

# Generate HTML report
newman run FleetSync_API_Collection.postman_collection.json \
  --reporters cli,html \
  --reporter-html-export report.html
```

## Continuous Monitoring

Set up a monitor in Postman to run these APIs every 5 minutes:

1. Click on the collection
2. Click **...** (three dots) → **Monitor Collection**
3. Set schedule (e.g., every 5 minutes)
4. Configure notifications for failures

## Troubleshooting

### Connection Refused
- **Cause**: Application not running
- **Solution**: Run `mvn spring-boot:run` in the project directory

### Kafka Health Returns "DOWN"
- **Cause**: Kafka not running
- **Solution**: Run `./start-kafka.sh` or `docker-compose up -d`

### Empty Truck List
- **Cause**: Simulator not started or no data yet
- **Solution**: Wait 5-10 seconds for data to flow through the pipeline

## Integration with CI/CD

Add to your CI pipeline:

```yaml
# .github/workflows/api-test.yml
name: API Tests
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start Application
        run: docker-compose up -d
      - name: Run API Tests
        run: |
          npm install -g newman
          newman run FleetSync_API_Collection.postman_collection.json
```
