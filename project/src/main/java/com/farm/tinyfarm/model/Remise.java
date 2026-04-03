package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="remise")
@Data
public class Remise {

    @Id
    private Integer remiseId;

    private int stockSavon = 0;
    private int stockSeringue = 0;
    private int stockPaille =0;
    //TODO  Liste des objets à collectionner
    
    @OneToOne
    @MapsId
    private Ferme ferme; //Partage le même id que la ferme

    public Remise() {} //Constructeur de base requis par JPA
}