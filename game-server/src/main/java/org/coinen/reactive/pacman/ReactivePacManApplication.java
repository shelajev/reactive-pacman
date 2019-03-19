package org.coinen.reactive.pacman;

import reactor.core.publisher.Hooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactivePacManApplication {

	public static void main(String[] args) {
		Hooks.onOperatorDebug();
		SpringApplication.run(ReactivePacManApplication.class, args);
	}

}
