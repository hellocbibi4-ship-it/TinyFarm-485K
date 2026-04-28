/*
 * Point d'entrée Spring Boot de TinyFarm. Lance l'application et configure le contexte d'exécution.
 */



package com.farm.tinyfarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TinyfarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(TinyfarmApplication.class, args);
	}
}
