package com.ztdx.eams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan(value = "com.ztdx.eams")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
