package com.farm.tinyfarm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farm.tinyfarm.model.Marche;

@Repository
public interface MarcheRepository extends JpaRepository<Marche, Integer> {
    List<Marche> findAllByOrderByPrixUnitaireAscIdOffreAsc();
}
