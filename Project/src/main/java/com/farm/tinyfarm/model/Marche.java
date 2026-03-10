package com.farm.tinyfarm.model;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "marche")
@Data
public class Marche{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation de idOffre
    private Integer idOffre;

    private String produit;
    private Integer quantite;
    private Integer prixUnitaire;

    @OneToOne
    @MapsId
    private Ferme ferme; //Partage le même id que la ferme
    public void setPrix(Integer prix){
        this.prixUnitaire = prix ;
    }

    public Integer getPrix(){
        return this.prixUnitaire;
    }

    public Ferme getFerme(){
        return this.ferme;
    }
    public Marche(){} //Constructeur par défaut pour JPA
}//class
