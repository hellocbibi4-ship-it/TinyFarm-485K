package com.farm.tinyfarm.model;
import java.time.LocalDateTime;

public class Ferme{

    private Integer idFerme;
    private Integer idUtilisateur;
    private String nom;
    private Integer soldeEcus;
    private Boolean hibernation;
    private LocalDateTime dateCreation;
    private Integer score;

    //====================== Getters ======================
    public Integer getIdFerme() {
        return idFerme;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public String getNom() {
        return nom;
    }

    public Integer getSoldeEcus() {
        return soldeEcus;
    }

    public Boolean getHibernation() {
        return hibernation;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Integer getScore() {
        return score;
    }

    //====================== Setters ======================
    public void setIdFerme(Integer idFerme) {
        this.idFerme = idFerme;
    }
    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setSoldeEcus(Integer soldeEcus) {
        this.soldeEcus = soldeEcus;
    }

    public void setHibernation(Boolean hibernation) {
        this.hibernation = hibernation;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

}//class
