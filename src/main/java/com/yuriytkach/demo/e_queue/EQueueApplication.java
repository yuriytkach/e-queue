package com.yuriytkach.demo.e_queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class EQueueApplication {

	public static void main(final String[] args) {
		SpringApplication.run(EQueueApplication.class, args);
	}

}
