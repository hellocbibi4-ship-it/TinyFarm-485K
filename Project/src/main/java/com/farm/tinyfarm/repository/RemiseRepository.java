package com.farm.tinyfarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.farm.tinyfarm.model.Remise;

@Repository
public interface RemiseRepository extends JpaRepository<Remise, Integer>{

}//Class