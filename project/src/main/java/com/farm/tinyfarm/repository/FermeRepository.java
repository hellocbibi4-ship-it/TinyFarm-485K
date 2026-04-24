package com.farm.tinyfarm.repository;

import java.util.List;

import com.farm.tinyfarm.model.Ferme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FermeRepository extends JpaRepository<Ferme, Integer>{

    List<Ferme> findAllByOrderByScoreDesc();

    List<Ferme> findAllByNomStartingWith(String prefix);
    List<Ferme> findAllByUtilisateurIsNull();
}//Class
