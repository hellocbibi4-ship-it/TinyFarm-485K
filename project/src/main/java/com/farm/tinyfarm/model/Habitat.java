/*
 * Entité de domaine JPA représentant habitat dans TinyFarm.
 */



package com.farm.tinyfarm.model;

import java.sql.Date;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "habitat")
@Data
public class Habitat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHabitat;

    @OneToOne
    @JoinColumn(name = "idFerme")
    private Ferme ferme;

    private TypeHabitat typeHabitat;
    private int capaMax;

    private boolean estSale = false;
    private boolean estMalade = false;
    private Date dateEstSale;

    @Transient
    private ArrayList<Animal> listeAnimaux = new ArrayList<>();

    public Habitat() {
        this.listeAnimaux = new ArrayList<>();
    }

    public Habitat(int capaMax, TypeHabitat typeHabitat) {
        this.listeAnimaux = new ArrayList<>();
        this.estSale = false;
        this.estMalade = false;
        this.dateEstSale = null;
        this.capaMax = capaMax;
        this.typeHabitat = typeHabitat;
    }

    public boolean estSale() { return estSale; }
    public boolean estMalade() { return estMalade; }
}