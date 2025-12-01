# Scaling FleetSync with Apache Kafka

Currently, **FleetSync** connects the MQTT Broker directly to the Spring Boot backend. This works great for 1,000 trucks. But what if you have **1 million trucks**?

That is where **Apache Kafka** comes in.

## The "MQTT + Kafka" Architecture

In a massive-scale system, we don't connect the backend directly to MQTT. Instead, we use Kafka as a massive buffer and router.

```mermaid
graph LR
    subgraph "IoT Edge (Connectivity)"
        Trucks -->|MQTT| HiveMQ[MQTT Broker]
    end

    subgraph "The Bridge"
        HiveMQ -->|Kafka Connect| Kafka[Apache Kafka]
    end

    subgraph "The Pipelines (Consumer Groups)"
        Kafka -->|Group A| Dashboard[Real-Time Dashboard]
        Kafka -->|Group B| DB[Database (History)]
        Kafka -->|Group C| AI[Fraud Detection AI]
    end
```

## How Kafka Manages "Lots of Pipelines"

Kafka allows you to run multiple **independent pipelines** from the exact same data stream without slowing each other down. This is done using **Consumer Groups**.

### 1. The "Hot" Pipeline (Real-Time Dashboard)
*   **Goal**: Show the truck on the map NOW.
*   **Mechanism**: A Spring Boot microservice subscribes to the `truck-telemetry` topic. It processes messages in memory and pushes to WebSockets. It doesn't save anything to disk. It needs to be fast.

### 2. The "Cold" Pipeline (Historical Analytics)
*   **Goal**: Save data to calculate "Average Speed Last Month".
*   **Mechanism**: A separate service (or Kafka Connect Sink) reads the *same* `truck-telemetry` topic. It writes every message to a database (PostgreSQL/InfluxDB).
*   **Magic**: If the database is slow, it *does not* slow down the Real-Time Dashboard. Kafka handles the speed difference.

### 3. The "Intelligence" Pipeline (Fraud/Theft)
*   **Goal**: Detect if a truck is stolen.
*   **Mechanism**: A Flink or Kafka Streams job reads the topic. It looks for complex patterns (e.g., "Engine on but GPS not moving for 20 mins").

## Why this is better than just MQTT?
1.  **Shock Absorber**: If 1 million trucks send data at once, MQTT might crash a direct database connection. Kafka "absorbs" the spike and lets the database catch up at its own pace.
2.  **Replayability**: If the "Fraud Detection" service crashes, you can fix it and "replay" the Kafka topic from 2 hours ago to catch what you missed. MQTT cannot do this (it's fire-and-forget).
3.  **Parallelism**: You can add a new "Billing Pipeline" next month without touching the existing Dashboard or Database code. You just add a new Consumer Group.
