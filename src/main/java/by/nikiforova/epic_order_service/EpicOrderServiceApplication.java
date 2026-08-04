package by.nikiforova.epic_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EpicOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpicOrderServiceApplication.class, args);
	}

}
