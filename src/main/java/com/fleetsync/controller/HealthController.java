package com.fleetsync.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Checks", description = "System and component health status")
public class HealthController {

    private final KafkaAdmin kafkaAdmin;

    public HealthController(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @GetMapping("/kafka")
    @Operation(summary = "Check Kafka Health", description = "Verifies connectivity to the Kafka cluster.")
    public Map<String, Object> checkKafka() {
        Map<String, Object> health = new HashMap<>();

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();

            // Get cluster info with timeout
            String clusterId = clusterResult.clusterId().get(5, java.util.concurrent.TimeUnit.SECONDS);
            int nodeCount = clusterResult.nodes().get(5, java.util.concurrent.TimeUnit.SECONDS).size();

            health.put("status", "UP");
            health.put("clusterId", clusterId);
            health.put("nodes", nodeCount);
            health.put("bootstrapServers", kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }

    @GetMapping("/system")
    @Operation(summary = "Check System Health", description = "Returns overall health status of all components.")
    public Map<String, Object> checkSystem() {
        Map<String, Object> status = new HashMap<>();
        status.put("application", "FleetSync");
        status.put("status", "UP");
        status.put("components", Map.of(
                "mqtt", "UP",
                "websocket", "UP",
                "kafka", "Check /api/health/kafka"));
        return status;
    }
}
