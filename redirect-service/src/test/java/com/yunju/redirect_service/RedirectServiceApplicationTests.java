package com.yunju.redirect_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestPropertySource(properties = {
		"spring.data.mongodb.port=0",
		"spring.mongodb.embedded.version=7.0.14"
})
class RedirectServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
