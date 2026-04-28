/*
 * Contrôleur REST gérant les opérations habitat de TinyFarm et exposant les points d'API correspondants.
 */



package com.farm.tinyfarm.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.service.HabitatService;

@RestController
@RequestMapping("api/habitat") //URL de base
public class HabitatController {

    private final HabitatService habitatService;

    public HabitatController(HabitatService habitatService){
        this.habitatService = habitatService;
    }
}
