package com.farm.tinyfarm.service;

import java.time.LocalDateTime;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.repository.FermeRepository;
import org.springframework.stereotype.Service;

@Service
public class FermeService {
    
    private final FermeRepository fermeRepository;

    public FermeService(FermeRepository fermeRepository){
        this.fermeRepository = fermeRepository;
    }

    public void validationPseudo(String pseudo){
        String alphabet = "[a-zA-Z0-9_-]{3,16}$";

        if(pseudo == null || !pseudo.matches(alphabet)){
            throw new IllegalArgumentException("Le nom doit faire entre 3 et 16 caractères et contenir (lettres maj/min, chiffres et '-' , '_')");
        }
    }

    public int create(Ferme ferme){
        ferme.setSoldeEcus(1500);
        validationPseudo(ferme.getNom());

        //En commentaire car le repository Utilisateur n'existe pas encore
        /*if (!existsUtilisateurById(ferme.getUtilisateur.getId())) { 
            throw new IllegalArgumentException("L'id de l'utilisateur de la ferme n'existe pas!");
        }*/
        ferme.setHibernation(false);
        ferme.setScore(0);
        ferme.setDateCreation(LocalDateTime.now());
        
        //Retourne 1 si réussite et 0 si échec
        return fermeRepository.createFerme(ferme);
    }
}
