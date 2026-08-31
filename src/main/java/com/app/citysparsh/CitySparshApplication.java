package com.app.citysparsh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class
CitySparshApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitySparshApplication.class, args);
	}

}
