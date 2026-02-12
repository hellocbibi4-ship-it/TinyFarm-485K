package com.farm.tinyfarm.repository;

import com.farm.tinyfarm.model.Ferme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FermeRepository extends JpaRepository<Ferme, Integer>{

}//Class
