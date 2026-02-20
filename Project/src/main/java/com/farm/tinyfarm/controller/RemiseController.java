package com.farm.tinyfarm.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/remise") // Toutes les URLS vont commencer par ca

public class RemiseController() {

    private final RemiseService remiseService;

    //Constructeur
    public RemiseController(RemiseService remiseService) {
        this.remiseService = remiseService;
    }
    //Création d'une ferme
    @PostMapping
    public ResponseEntity<Remise> creerRemise(@RequestBody Remise remise) {
        Remise newRemise = remiseService.creerRemise(remise);
        return new ResponseEntity<>(newFerme, HttpStatus.CREATED);
    }

}//Class

