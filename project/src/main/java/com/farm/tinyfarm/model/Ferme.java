package com.farm.tinyfarm.model;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "ferme")
@Data
public class Ferme{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation de idFerme
    private Integer idFerme;

    @OneToOne
    @JoinColumn(name="idUtilisateur", unique=true)
    private Utilisateur utilisateur;

    private String nom;
    private Integer soldeEcus;
    private Boolean hibernation;
    private LocalDateTime dateCreation;
    private Integer score;
    private LocalDateTime derniereCo;
    private LocalDateTime dernierePonteOeufs;
    private Integer nbVaches = 1;
    private Integer nbPoules = 3;
    private Integer nbLapins = 2;
    private Integer nbLapinsMalades = 0;
    private Integer nbVachesAffamees = 0;
    private Integer nbVachesAssoiffees = 0;
    private Integer nbPouleAffamees = 0;
    private Integer nbPouleAssoiffees = 0;
    private Integer nbLapinsAffames = 0;
    private Integer nbLapinsAssoiffes = 0;
    @OneToOne(mappedBy = "ferme", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Remise remise;
    public Ferme(){} //Constructeur par défaut pour JPA
}//class
