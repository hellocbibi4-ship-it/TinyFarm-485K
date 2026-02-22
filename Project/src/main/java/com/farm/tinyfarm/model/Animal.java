package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

        @OneToOne
        @JoinColumn(name = "idFerme", unique = true)
        private Ferme ferme;

        /*@OneToOne
        @JoinColumn(name = "idHabitat", unique = "true")
        private Habitat habitat;*/

        private String nom;
        private TypeAnimal typeAnimal;
        private TypeSexe sexe;
        private TypeStade stade;
        private TypeRole role;

        private float poids;
        private int age = 0;
        private int jaugeSante = 100;
        private int jaugeFaim = 100;
        private int jaugeProprete = 100;
        private int jaugeHydratation = 100;
        
        private boolean estMalade = false;
        private boolean aMange = false;
        private boolean aEteTraite = false;

        public Animal(){} //Constructeur par défaut nécessaire pour JPA

        public boolean estMalade() {
            return estMalade;
        }

}//Class