package com.uwc.bmbrmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BmbrmnApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmbrmnApplication.class, args);
	}

}
