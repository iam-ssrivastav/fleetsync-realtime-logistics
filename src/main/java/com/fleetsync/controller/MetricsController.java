package com.fleetsync.controller;

import java.lang.management.ManagementFactory;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final KafkaAdmin kafkaAdmin;

    public MetricsController(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @GetMapping("/kafka/consumer")
    public Map<String, Object> getKafkaConsumerMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            String groupId = "fleetsync-dashboard";

            // Get consumer group description
            ConsumerGroupDescription groupDesc = adminClient.describeConsumerGroups(Collections.singleton(groupId))
                    .all()
                    .get(5, TimeUnit.SECONDS)
                    .get(groupId);

            // Get offsets
            ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> offsets = offsetsResult.partitionsToOffsetAndMetadata()
                    .get(5, TimeUnit.SECONDS);

            // Build metrics
            metrics.put("groupId", groupId);
            metrics.put("state", groupDesc.state().toString());
            metrics.put("members", groupDesc.members().size());

            List<Map<String, Object>> partitionMetrics = new ArrayList<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                Map<String, Object> partition = new HashMap<>();
                partition.put("topic", entry.getKey().topic());
                partition.put("partition", entry.getKey().partition());
                partition.put("currentOffset", entry.getValue().offset());
                partition.put("metadata", entry.getValue().metadata());
                partitionMetrics.add(partition);
            }
            metrics.put("partitions", partitionMetrics);

            // Member details
            List<Map<String, String>> memberDetails = new ArrayList<>();
            for (MemberDescription member : groupDesc.members()) {
                Map<String, String> memberInfo = new HashMap<>();
                memberInfo.put("memberId", member.consumerId());
                memberInfo.put("clientId", member.clientId());
                memberInfo.put("host", member.host());
                memberDetails.add(memberInfo);
            }
            metrics.put("memberDetails", memberDetails);

        } catch (Exception e) {
            metrics.put("error", e.getMessage());
        }

        return metrics;
    }

    @GetMapping("/kafka/topics")
    public Map<String, Object> getKafkaTopics() {
        Map<String, Object> result = new HashMap<>();

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> topics = adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            result.put("topics", topics);
            result.put("count", topics.size());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    @GetMapping("/realtime")
    public Map<String, Object> getRealtimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // System info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> system = new HashMap<>();
        system.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        system.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        system.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        system.put("processors", runtime.availableProcessors());
        system.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        metrics.put("system", system);
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("application", "FleetSync");

        return metrics;
    }
}
