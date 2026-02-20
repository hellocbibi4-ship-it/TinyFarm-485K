package com.farm.tinyfarm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.service.RemiseService;

@RestController
public class RemiseController {
    private final RemiseService remiseService;

    //Constructeur
    public RemiseController(RemiseService remiseService) {
        this.remiseService = remiseService;
    }
    //Création d'une ferme
    @PostMapping
    public ResponseEntity<Remise> creerRemise(@RequestBody Remise remise) {
        Remise newRemise = remiseService.createRemise(remise);
        return new ResponseEntity<>(newRemise, HttpStatus.CREATED);
    }
}

/*package com.farm.tinyfarm.controller;

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

 */