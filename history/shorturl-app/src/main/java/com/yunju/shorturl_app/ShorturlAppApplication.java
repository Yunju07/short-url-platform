package com.yunju.shorturl_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ShorturlAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShorturlAppApplication.class, args);
	}

}
