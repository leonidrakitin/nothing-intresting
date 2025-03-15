package ru.sushi.delivery.kds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KdsApplication.class, args);
	}

}
