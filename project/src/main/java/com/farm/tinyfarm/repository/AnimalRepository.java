package com.farm.tinyfarm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.farm.tinyfarm.model.Animal;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer>{

    List<Animal> findByFerme_IdFerme(Integer idFerme);

}//Class
