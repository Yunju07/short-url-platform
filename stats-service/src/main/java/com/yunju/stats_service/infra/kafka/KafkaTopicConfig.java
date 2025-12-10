package com.yunju.stats_service.infra.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                "shorturl-kafka-1:19093,shorturl-kafka-2:19094,shorturl-kafka-3:19095");
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic clickLogDlqTopic() {
        return TopicBuilder.name("shorturl.click-log.dlq")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
