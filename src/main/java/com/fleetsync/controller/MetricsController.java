package com.fleetsync.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics APIs", description = "System and Kafka performance metrics")
public class MetricsController {

    private final KafkaAdmin kafkaAdmin;

    public MetricsController(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @GetMapping("/kafka/topics")
    @Operation(summary = "Get Kafka Topics", description = "Lists all available Kafka topics.")
    public Set<String> getKafkaTopics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topics = adminClient.listTopics();
            return topics.names().get();
        } catch (InterruptedException | ExecutionException e) {
            return Collections.singleton("Error fetching topics: " + e.getMessage());
        }
    }

    @GetMapping("/kafka/consumer")
    @Operation(summary = "Get Consumer Metrics", description = "Returns metrics for the 'fleetsync-dashboard' consumer group.")
    public Map<String, Object> getConsumerMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("groupId", "fleetsync-dashboard");
        metrics.put("state", "Stable");
        metrics.put("members", 1);
        metrics.put("currentOffset", new Random().nextInt(1000)); // Simulated for demo
        return metrics;
    }

    @GetMapping("/realtime")
    @Operation(summary = "Get System Metrics", description = "Returns real-time JVM and system resource usage.")
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        metrics.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
        metrics.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
        metrics.put("systemLoad", osBean.getSystemLoadAverage());
        metrics.put("availableProcessors", osBean.getAvailableProcessors());
        metrics.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        return metrics;
    }
}
