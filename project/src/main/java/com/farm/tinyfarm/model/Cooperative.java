package com.farm.tinyfarm.model;

import java.lang.annotation.Inherited;

import jakarta.persistence.*;

@Entity
@Table(name = "cooperative")
public class Cooperative{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false)
    private Integer prix;
    public Cooperative(){
    }
    public Cooperative(String produit, Integer quantite, Integer prix) {
        this.produit = produit;
        this.quantite = quantite;
        this.prix= prix;
    }
    public Integer getId(){
        return id;
    }
    public void setId(Integer id){
        this.id= id;
    }
    public void setProduit(String produit){
        this.produit = produit;
    }
    public Integer getQuantite(){
        return this.quantite;
    }
    public String getProduit(){
        return this.produit;
    }
    public void setQuantite(Integer quantite){
        this.quantite = quantite;
    }
    public Integer getPrix(){
        return prix;
    }
    public void setPrix(Integer prix){
        this.prix = prix;
    }
}