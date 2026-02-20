package java.com.farm.tinyfarm.service;

import java.com.farm.tinyfarm.repository.RemiseRepository;
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