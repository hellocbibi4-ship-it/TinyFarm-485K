package com.farm.tinyfarm.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "utilisateur")
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUtilisateur;

    @Column(unique = true, nullable = false)
    private String githubUsername; 

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Ferme ferme;
    private String role;

    // Constructeur par défaut obligatoire pour JPA
    public Utilisateur() {} 
}