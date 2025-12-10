package com.yunju.stats_service.infra.kafka;

import com.yunju.stats_service.global.event.ShortUrlClickedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaTemplate<String, Object> dlqKafkaTemplate;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShortUrlClickedEvent> kafkaListenerContainerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "shorturl-kafka-1:19093,shorturl-kafka-2:19094,shorturl-kafka-3:19095");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "stats-consumer");

        props.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, "classic");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 안정적 리밸런싱을 위한 설정
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        // 메시지 처리 중 지연 대비
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5분
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);           // 안정성 ↑

        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ShortUrlClickedEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.yunju.stats_service.*");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        DefaultKafkaConsumerFactory<String, ShortUrlClickedEvent> factory =
                new DefaultKafkaConsumerFactory<>(props);

        /* ------- Listener Container 설정 ------- */

        ConcurrentKafkaListenerContainerFactory<String, ShortUrlClickedEvent> container =
                new ConcurrentKafkaListenerContainerFactory<>();

        container.setConsumerFactory(factory);
        container.setConcurrency(3);

        // 수동 커밋 모드
        container.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.BATCH);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition("shorturl.click-log.dlq", record.partition())
        );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(3000L, 1)); // 1회 재시도, 3초 간격

        container.setCommonErrorHandler(errorHandler);

        return container;
    }
}
