package com.farm.tinyfarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;

import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.CooperativeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Cooperative;
import com.farm.tinyfarm.model.Remise;

@SpringBootApplication
public class TinyfarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(TinyfarmApplication.class, args);
	}
	@Bean
    public CommandLineRunner initialiserDonnees(FermeRepository acheteurRepo, CooperativeRepository articleRepo, RemiseRepository remiseRepo) {
        return args -> {
            // Création de l'acheteur ID 1
            Ferme f = new Ferme();
			
			f.setNom("Jean Mich");
			f.setSoldeEcus(1000); 
			f.setHibernation(false);
			f.setDateCreation(LocalDateTime.now());
			acheteurRepo.save(f);

			Remise r = new Remise();
			r.setFerme(f);
			remiseRepo.save(r);

            // Création de l'article ID 10
            Cooperative article = new Cooperative();
            article.setProduit("PAILLE");
            article.setPrix(2);
            article.setQuantite(50);
            articleRepo.save(article);

            System.out.println(">>> BASE DE DONNÉES INITIALISÉE POUR LES TESTS PYTEST <<<");
        };
    }
}
