package com.polypadel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolypadelApplication {
	public static void main(String[] args) {
		SpringApplication.run(PolypadelApplication.class, args);
	}
}
