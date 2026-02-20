package com.farm.tinyfarm.service;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.repository.RemiseRepository;

@Service
public class RemiseService {

    private final RemiseRepository remiseRepository;

    public RemiseService(RemiseRepository remiseRepository){
        this.remiseRepository = remiseRepository;
    }

     //Fonction de création d'une remise
    public Remise createRemise(Remise remise){
        remise.setStockSavon(0);
        remise.setStockSeringue(0);
        remise.setStockPaille (0);
        return remiseRepository.save(remise);
    }
}//Class

/* 
package com.farm.tinyfarm.service;

import java.com.farm.tinyfarm.repository.RemiseRepository;

import org.springframework.stereotype.Service;

import java.com.farm.tinyfarm.model.Remise;

@Service
public class RemiseService() {

    private final RemiseRepository remiseRepository;

    public RemiseService(RemiseRepository remiseRepository){
        this.remiseRepository = remiseRepository;
    }

    //Fonction de création d'une remise
    public Remise createRemise(Remise remise){
        remise.setStockSavon = 0;
        remise.setStockSeringue = 0;
        remise.setStockPaille =0;
        return remiseRepository.save(remise);
    }
}//Class
*/