package com.farm.tinyfarm.outils;

import com.farm.tinyfarm.model.*;

public class Utilitaires {
    
    public static void validationNom(String nom){
        String alphabet = "^[a-zA-Z0-9_-]{3,16}$";

        if(nom == null || !nom.matches(alphabet)){
            throw new IllegalArgumentException("Le nom doit faire entre 3 et 16 caractères et contenir (lettres maj/min, chiffres et '-' , '_')");
        }
    }

    public TypeSexe generateRandomGender(){
        int rand = (int) Math.random() * 2; //Génére un int entre 0 et 1
        if(rand == 1) { return TypeSexe.FEMELLE;}
        else{ return TypeSexe.MALE; }
    }
}
