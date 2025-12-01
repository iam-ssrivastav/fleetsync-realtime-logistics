# FleetSync: Project Explanation Guide

## 1. Project Overview (The "Elevator Pitch")
"FleetSync is a **Real-Time IoT Logistics Tracking System**. It allows fleet managers to monitor the exact location, speed, and health of delivery trucks in real-time on a live map. It uses **MQTT** for high-frequency data ingestion from devices and **WebSockets** to stream that data instantly to a web dashboard."

## 2. The Problem It Solves
Traditional tracking systems often use **HTTP Polling** (the app asks the server "Where is the truck?" every few seconds). This has three major problems:
1.  **High Latency**: You only see where the truck *was* 10 seconds ago, not where it is *now*.
2.  **Server Load**: If you have 10,000 trucks, handling 10,000 HTTP requests per second crashes standard servers.
3.  **Bandwidth**: HTTP headers are heavy. For IoT devices with poor signal (like trucks in tunnels), sending heavy data drains battery and fails often.

**FleetSync solves this** by using an Event-Driven Architecture that is lightweight, instant, and scalable.

## 3. Architecture & Tech Stack

### A. The "Edge" (MQTT)
*   **Technology**: MQTT (Message Queuing Telemetry Transport).
*   **Role**: This is the communication protocol for the trucks.
*   **Why?**: It's extremely lightweight (binary protocol). It works well on unstable 3G/4G networks. If a truck disconnects, MQTT handles the reconnection automatically.

### B. The "Brain" (Spring Boot)
*   **Technology**: Spring Boot + Spring Integration MQTT.
*   **Role**: The backend subscribes to the MQTT broker. It acts as a bridge.
*   **Logic**: It receives raw data, parses it, and checks for critical events (e.g., "Is Speed > 80mph?"). If an issue is found, it triggers an alert.

### C. The "View" (WebSockets & Frontend)
*   **Technology**: STOMP over WebSockets, Leaflet.js (Maps), Chart.js.
*   **Role**: The dashboard for the user.
*   **Why WebSockets?**: We don't want the user to hit "Refresh". The server **pushes** data to the browser the millisecond it arrives. This creates a "Stock Ticker" style experience.

## 4. Data Flow (The "Life of a Packet")
1.  **Generation**: A `TruckSimulator` generates a telemetry packet: `{ "id": "T1", "speed": 85, "lat": 40.71, "lon": -74.00 }`.
2.  **Publish**: The truck publishes this to the topic `fleet/trucks/T1` on the MQTT Broker.
3.  **Ingest**: Spring Boot (listening on `fleet/trucks/+`) receives the message immediately.
4.  **Process**: The `TelemetryService` checks the speed. It sees 85mph > 80mph limit. It creates a `SPEEDING` alert.
5.  **Broadcast**: The service pushes the telemetry to `/topic/telemetry` and the alert to `/topic/alerts` via WebSockets.
6.  **Visualize**: The frontend receives the message. Leaflet.js moves the marker on the map, and the Alert Feed shows a warning—all in under 100ms.

## 5. Key Technical Challenges Solved
*   **Concurrency**: Handling multiple streams of data simultaneously without blocking.
*   **Real-Time Visualization**: Smoothly animating markers on a map instead of them "jumping" around.
*   **Decoupling**: The trucks don't know the dashboard exists. They just talk to the broker. This means we can add 10 new dashboards (mobile app, analytics engine) without changing the truck software.

## 6. Future Enhancements (To show you think big)
*   **Geofencing**: Alerting if a truck leaves a designated route.
*   **Persistence**: Saving data to a Time-Series Database (InfluxDB) for historical replay.
*   **Bi-Directional Control**: Sending commands back to the truck (e.g., "Remote Engine Cut-off").
