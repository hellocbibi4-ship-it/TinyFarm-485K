package com.farm.tinyfarm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.service.AnimalService;
import com.farm.tinyfarm.model.Ferme;
import static org.mockito.Mockito.mock;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.model.TypeStade;

@SpringBootTest
class AnimalServiceTests {
    @Test
    void contextLoads() {
    }


    @Test
    void testNourrirPoule() {
        // Creer une ferme de test 
        Ferme fermeTest = new Ferme();
        fermeTest.setNom("FermeTest");

        // Creer une poule de test
        Animal animal = new Animal();
        animal.setTypeAnimal(TypeAnimal.POULE);
        animal.setJaugeFaim(50);
        animal.setFerme(fermeTest); // Associe l'animal à la ferme
        animal.setStade(TypeStade.ADULTE);

        // Mock du FermeService afin d'éviter de passer des paramètres null au constructeur d'AnimalService
        FermeService fermeServiceMock = mock(FermeService.class);
        AnimalRepository animalRepositoryMock = mock(AnimalRepository.class);
        AnimalService animalService = new AnimalService(animalRepositoryMock, fermeServiceMock);

        animalService.nourrirPoule(animal);

        // assert que la jauge de faim de la poule est à 100 après avoir été nourrie
        assertEquals(100, animal.getJaugeFaim());
    }
}


