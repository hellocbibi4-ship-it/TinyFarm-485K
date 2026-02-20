package com.farm.tinyfarm.service;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;

import jakarta.transaction.Transactional;

@Service
public class RemiseService {

    private final RemiseRepository remiseRepository;
    private final FermeRepository fermeRepository;

    public RemiseService(RemiseRepository remiseRepository, FermeRepository fermeRepository){
        this.remiseRepository = remiseRepository;
        this.fermeRepository = fermeRepository;
    }

     //Fonction de création d'une remise
    public Remise createRemise(Integer fermeId) {
        Ferme ferme = fermeRepository.findById(fermeId)
            .orElseThrow(() -> new RuntimeException("Ferme non trouvée"));
        
        Remise remise = new Remise();
        remise.setFerme(ferme); // @MapsId récupère l'ID

        remise.setStockSavon(0);
        remise.setStockSeringue(0);
        remise.setStockPaille (0);
        return remiseRepository.save(remise);
    }

    @Transactional
    public void ajouterStock(Integer idRemise, TypeStock t, int montant) {

        Remise remise = remiseRepository.findById(idRemise)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter le score: la remise n'existe pas"));;
        
        if(montant <= 0) {
            throw new IllegalArgumentException("Impossible d'ajouter du stock, le montant doit être de 1 minimum");
        }

        switch (t) {
            case PAILLE :
                int montantTotal = remise.getStockPaille() + montant;
                remise.setStockPaille(montantTotal);
                break;
            case SAVON :
                montantTotal = remise.getStockSavon() + montant;
                remise.setStockSavon(montantTotal);
                break;
            case SERINGUE :
                montantTotal = remise.getStockSeringue() + montant;
                remise.setStockSeringue(montantTotal);
                break;
        }
    }

    public Remise getById(Integer id) {
        return remiseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
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