#!/bin/bash

echo "🧹 Cleaning up existing containers..."
docker rm -f zookeeper kafka 2>/dev/null || true

echo "🚀 Starting Kafka infrastructure..."
docker-compose up -d

echo "⏳ Waiting for Kafka to be ready (15 seconds)..."
sleep 15

echo "✅ Kafka is ready! You can now run: mvn spring-boot:run"
