/*
 * Entité de domaine JPA représentant marche dans TinyFarm.
 */



package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "marche")
@Data
public class Marche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOffre;

    private String produit;
    private Integer quantite;
    private Integer prixUnitaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ferme_id_ferme")
    private Ferme ferme;

    public void setPrix(Integer prix) {
        this.prixUnitaire = prix;
    }

    public Integer getPrix() {
        return this.prixUnitaire;
    }

    public Marche() {}
}
