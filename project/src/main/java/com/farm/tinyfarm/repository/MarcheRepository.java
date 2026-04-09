package com.farm.tinyfarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.farm.tinyfarm.model.Marche;

@Repository
public interface MarcheRepository extends JpaRepository<Marche, Integer>{

}//Class