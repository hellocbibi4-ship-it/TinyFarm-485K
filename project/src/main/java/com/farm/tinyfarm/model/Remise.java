package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "remise")
@Data
public class Remise {

    @Id
    private Integer remiseId;

    private int stockOeuf = 0;
    private int stockLait = 0;
    private int stockLapin = 0;
    private int stockNourriture = 0;
    private int stockEau = 0;
    private int stockSavon = 0;
    private int stockSeringue = 0;
    private int stockPaille =0;
    
    @OneToOne
    @MapsId
    private Ferme ferme;

    public Remise() {}
}
