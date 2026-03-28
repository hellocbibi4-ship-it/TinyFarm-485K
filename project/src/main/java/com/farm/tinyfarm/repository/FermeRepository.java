package com.farm.tinyfarm.repository;

import java.util.List;

import com.farm.tinyfarm.model.Ferme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FermeRepository extends JpaRepository<Ferme, Integer>{

    List<Ferme> findAllByOrderByScoreDesc();

}//Class
