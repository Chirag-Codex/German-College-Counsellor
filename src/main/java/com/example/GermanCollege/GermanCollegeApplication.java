package com.example.GermanCollege;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GermanCollegeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GermanCollegeApplication.class, args);
	}

}
