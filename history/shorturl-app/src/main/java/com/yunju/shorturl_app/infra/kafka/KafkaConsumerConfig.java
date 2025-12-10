package com.yunju.shorturl_app.infra.kafka;

import com.yunju.shorturl_app.global.event.ShortUrlClickedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShortUrlClickedEvent> kafkaListenerContainerFactory() {

        Map<String, Object> props = new HashMap<>();

        /* ------- 핵심 Consumer 설정 ------- */

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "shorturl-kafka:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "shorturl-click-consumer");

        // Kafka 최신 버전에서 권장: CLASSIC 고정
        props.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, "classic");

        // 메시지 처음 받을 때 안전하게 earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 자동 커밋 OFF → 수동 커밋(BATCH)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 안정적 리밸런싱을 위한 설정
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        // 메시지 처리 중 지연 대비
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5분
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);           // 안정성 ↑

        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ShortUrlClickedEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.yunju.shorturl_app.*");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, ShortUrlClickedEvent> factory =
                new DefaultKafkaConsumerFactory<>(
                        props
                );

        /* ------- Listener Container 설정 ------- */

        ConcurrentKafkaListenerContainerFactory<String, ShortUrlClickedEvent> container =
                new ConcurrentKafkaListenerContainerFactory<>();

        container.setConsumerFactory(factory);
        container.setConcurrency(3);

        // 수동 커밋 모드
        container.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.BATCH);

        return container;
    }
}
