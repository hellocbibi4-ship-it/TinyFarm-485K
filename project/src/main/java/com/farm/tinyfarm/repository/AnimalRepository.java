package com.farm.tinyfarm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer>{
    List<Animal> findByFerme_IdFermeOrderByIdAnimalAsc(Integer fermeId);
    List<Animal> findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(Integer fermeId, TypeAnimal typeAnimal);
    long countByFerme_IdFermeAndTypeAnimal(Integer fermeId, TypeAnimal typeAnimal);
}//Class
