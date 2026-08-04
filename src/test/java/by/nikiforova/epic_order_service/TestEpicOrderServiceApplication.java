package by.nikiforova.epic_order_service;

import org.springframework.boot.SpringApplication;

public class TestEpicOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(EpicOrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
