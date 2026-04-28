/*
 * Interface Spring Data JPA pour la persistance des entités habitat de TinyFarm.
 */



package com.farm.tinyfarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farm.tinyfarm.model.Habitat;

@Repository
public interface HabitatRepository extends JpaRepository<Habitat, Integer>{

}//Class
