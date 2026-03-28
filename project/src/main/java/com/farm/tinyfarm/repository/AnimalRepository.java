package com.farm.tinyfarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.farm.tinyfarm.model.Animal;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer>{

}//Class
