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
    @OneToOne
    @JoinColumn(name="fermeId", unique=true);

}
