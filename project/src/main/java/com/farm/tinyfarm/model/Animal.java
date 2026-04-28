/*
 * Entité de domaine JPA représentant animal dans TinyFarm.
 */



package com.farm.tinyfarm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Entity
@Table(name = "animal")
@Data
public class Animal{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrémentation de idAnimal
        private Integer idAnimal;

        @ManyToOne
        @JoinColumn(name = "idFerme")
        @JsonIgnore
        private Ferme ferme;

        /*@OneToOne
        @JoinColumn(name = "idHabitat", unique = "true")
        private Habitat habitat;*/

        private String nom;

        @Enumerated(EnumType.STRING)
        private TypeAnimal typeAnimal;

        @Enumerated(EnumType.STRING)
        private TypeSexe sexe;

        @Enumerated(EnumType.STRING)
        private TypeStade stade;

        @Enumerated(EnumType.STRING)
        private TypeRole role;

        private float poids;
        private int age = 0;
        private int jaugeSante = 100;
        private int jaugeFaim = 100;
        private int jaugeProprete = 100;
        private int jaugeHydratation = 100;
        private int joursMaladeConsecutifs = 0;
        private int joursJeuneConsecutifs = 0;
        
        private boolean estMalade = false;
        private boolean aMange = false;
        private boolean aBu = false;
        private boolean aEteTraite = false;
        private int stockLaitPis = 0;
        private int stockLaitPisMax = 16;
        private int nbJoursSansNourriture = 0;
        private int nbJoursSansHydratation = 0;
        private int nbJoursMalade = 0;
        private boolean vivant = true;

        public Animal(){} //Constructeur par défaut nécessaire pour JPA

        public boolean estMalade() {
            return estMalade;
        }

}//Class
