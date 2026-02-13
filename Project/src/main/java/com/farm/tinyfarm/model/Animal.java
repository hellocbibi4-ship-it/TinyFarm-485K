package com.farm.tinyfarm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "animal")
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
        private float poids;
        private int age;
        private TypeSexe sexe;
        private TypeStade stade;
        private boolean estMalade;
        private int jaugeSante = 100;
        private int jaugeFaim = 100;
        private int jaugeProprete = 100;
        private int jaugeHydratation = 100;

        //====================== Getters ======================
        public Integer getIdAnimal() {
                return idAnimal;
        }

        public Integer getIdFerme() {
                return ferme.getIdFerme();
        }

        public String getNom() {
                return nom;
        }   

        public TypeAnimal getTypeAnimal() {
                return typeAnimal;
        }

        public float getPoids() {
                return poids;
        }

        public int getAge() {
                return age;
        }
        public TypeSexe getSexe() {
                return sexe;
        }
        public TypeStade getStade() {
                return stade;
        }
        public boolean EstMalade() {
                return estMalade;
        }
        public int getJaugeSante() {
                return jaugeSante;
        }
        public int getJaugeFaim() {
                return jaugeFaim;
        }
        public int getJaugeProprete() {
                return jaugeProprete;
        }
        public int getJaugeHydratation() {
                return jaugeHydratation;
        }       

        //====================== Setters ======================

        public void setNom(String nom) {
                this.nom = nom;
        }
        public void setTypeAnimal(TypeAnimal typeAnimal) {
                this.typeAnimal = typeAnimal;
        }
        public void setPoids(float poids) {
                this.poids = poids;
        }
        public void setAge(int age) {
                this.age = age;
        }
        public void setSexe(TypeSexe sexe) {
                this.sexe = sexe;
        }
        public void setStade(TypeStade stade) {
                this.stade = stade;
        }
        public void setEstMalade(boolean estMalade) {
                this.estMalade = estMalade;
        }
        public void setJaugeSante(int jaugeSante) {
                this.jaugeSante = jaugeSante;
        }
        public void setJaugeFaim(int jaugeFaim) {
                this.jaugeFaim = jaugeFaim;
        }
        public void setJaugeProprete(int jaugeProprete) {
                this.jaugeProprete = jaugeProprete;
        }
        public void setJaugeHydratation(int jaugeHydratation) {
                this.jaugeHydratation = jaugeHydratation;
        }

        
        

}//Class