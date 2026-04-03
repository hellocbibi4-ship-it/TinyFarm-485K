package com.farm.tinyfarm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.farm.tinyfarm.model.Cooperative;

@Repository
public interface CooperativeRepository extends JpaRepository<Cooperative, Integer>{

}//Class