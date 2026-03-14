package com.farm.tinyfarm.model;

import java.sql.Date;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "habitat")
@Data
public class Habitat {

    //Attributs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation de idAnimal
    private Integer idHabitat;

    private TypeHabitat typeHabitat;
    private ArrayList<Animal> listeAnimaux;

    private int capaMax;

    //Pour le clapier
    private boolean estSale;
    private boolean estMalade;
    private Date dateEstSale; //Date à laquelle le clapier est devenu sale

    //Constructeur
    public Habitat() {}

    public boolean estSale() {
        return estSale;
    }

    public boolean estMalade() {
        return estMalade;
    }

    public Date getDateEstSale() {
        return dateEstSale;
    }

    public int getCapaMax() {
        return capaMax;
    }

}//CLass
