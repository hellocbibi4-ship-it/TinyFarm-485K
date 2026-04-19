package com.farm.tinyfarm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.service.AnimalService;
import com.farm.tinyfarm.model.Ferme;
import static org.mockito.Mockito.mock;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;

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

    @Test
    void testCreateBaseAnimalPoule() {
        FermeService fermeServiceMock = mock(FermeService.class);
        AnimalRepository animalRepositoryMock = mock(AnimalRepository.class);
        AnimalService animalService = new AnimalService(animalRepositoryMock, fermeServiceMock);

        Animal animal = new Animal();
        animal.setNom("PouleTest");
        animal.setTypeAnimal(TypeAnimal.POULE);

        animalService.createBaseAnimal(animal);

        assertEquals(0, animal.getAge());
        assertEquals(0.05f, animal.getPoids());
        assertEquals(TypeStade.ENFANT, animal.getStade());
        assertEquals(TypeRole.ELEVAGE, animal.getRole());
        assertEquals(TypeSexe.INCONNU, animal.getSexe());
        assertTrue(animal.isVivant());
    }

    @Test
    void testUpdateChickenStatusRevealsRoleAtAgeFive() {
        FermeService fermeServiceMock = mock(FermeService.class);
        AnimalRepository animalRepositoryMock = mock(AnimalRepository.class);
        AnimalService animalService = new AnimalService(animalRepositoryMock, fermeServiceMock);

        Animal animal = new Animal();
        animal.setNom("PouleAdulte");
        animal.setTypeAnimal(TypeAnimal.POULE);
        animal.setAge(5);
        animal.setPoids(2.5f);
        animal.setStade(TypeStade.ADULTE);
        animal.setSexe(TypeSexe.MALE);

        animalService.updateChickenStatus(animal);

        assertEquals(TypeRole.REPRODUCTEUR, animal.getRole());
        assertNotNull(animal.getSexe());
    }
}


