package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="remise")
@Data
public class Remise {

    @Id
    private Integer id;

    

    private int savon = 0;
    private int seringue = 0;
    private int paille =0;
    //TODO  Liste des objets à collectionner
    
    @OneToOne
    @MapsId
    private Ferme ferme; //Partage le même id que la ferme

    public Remise() {} //Constructeur de base requis par JPA
}