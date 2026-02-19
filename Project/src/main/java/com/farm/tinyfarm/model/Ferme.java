package com.farm.tinyfarm.model;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ferme")
@Data
public class Ferme{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation de idFerme
    private Integer idFerme;

    //@OneToOne
    //@JoinColumn(name="idUtilisateur", unique=true)
    //private Utilisateur utilisateur;

    private String nom;
    private Integer soldeEcus;
    private Boolean hibernation;
    private LocalDateTime dateCreation;
    private Integer score;

    public Ferme(){} //Constructeur par défaut pour JPA
}//class
