package com.farm.tinyfarm.model;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
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
    private Boolean hibernation;
    private LocalDateTime dateCreation;
    private Integer score;
    private Integer achatsJour = 0; // pour limiter les achats à 12 par jour
    private LocalDate dateDernierAchat;
    private LocalDateTime derniereCo;
    private Integer jourActuel = 1;
    private Integer achatsCollectiviteRestants = 12;
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
    private Double VOeuf = 0.0;
    private Double VLapCop = 0.0;
    private Double VPouleCop = 0.0;
    private Double VLact = 0.0;
    private Double VLaine = 0.0;
    private Double PChamps = 0.0;
    private Double PPot = 0.0;
    private Double PFood = 0.0;
    private Double BElev = 0.0;
    private Double VElev = 0.0;
    private Double BBlack = 0.0;
    private Double VBLack = 0.0;
    private Integer soldeEcus;
    @OneToOne(mappedBy = "ferme", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Remise remise;

    @OneToMany(mappedBy = "ferme", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Animal> animals = new ArrayList<>();

    public Ferme(){} //Constructeur par défaut pour JPA
}//class
