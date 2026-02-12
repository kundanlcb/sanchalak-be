package com.cm.sanchalak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableJpaAuditing
public class SanchalakApplication {

	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(SanchalakApplication.class, args);
	}

}
