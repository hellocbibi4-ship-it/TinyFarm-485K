package com.farm.tinyfarm.outils;

public class Utilitaires {
    
    public void validationNom(String nom){
        String alphabet = "^[a-zA-Z0-9_-]{3,16}$";

        if(nom == null || !nom.matches(alphabet)){
            throw new IllegalArgumentException("Le nom doit faire entre 3 et 16 caractères et contenir (lettres maj/min, chiffres et '-' , '_')");
        }
    }
}
