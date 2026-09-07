package com.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ACollaborativeTaskTrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ACollaborativeTaskTrackingSystemApplication.class, args);
	}

}
