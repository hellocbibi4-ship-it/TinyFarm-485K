package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.outils.Utilitaires;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class FermeService {
    public static final int GAME_DAY_DURATION_SECONDS = 60;

    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;

    public FermeService(FermeRepository fermeRepository, RemiseRepository remiseRepository) {
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
    }

    public Ferme create(Ferme ferme) {
        Utilitaires.validationNom(ferme.getNom());
        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        ferme.setDerniereCo(LocalDateTime.now());
        ferme.setDernierePonteOeufs(LocalDateTime.now());
        ferme.setNbVaches(1);
        ferme.setNbPoules(3);
        ferme.setNbLapins(2);

        Ferme savedFerme = fermeRepository.save(ferme);

        if (!remiseRepository.existsById(savedFerme.getIdFerme())) {
            Remise remise = new Remise();
            remise.setFerme(savedFerme);
            remiseRepository.save(remise);
        }

        return savedFerme;
    }

    public void deleteById(Integer id) {
        fermeRepository.deleteById(id);
    }

    @Transactional
    public void ajouterEcus(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les ecus: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le montant d'ecus a ajouter doit etre positif");
        }

        int montantTotal = ferme.getSoldeEcus() + montant;
        ferme.setSoldeEcus(montantTotal);
    }

    @Transactional
    public void retirerEcus(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les ecus: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le montant d'ecus a retirer doit etre positif");
        }

        int montantTotal = ferme.getSoldeEcus() - montant;
        ferme.setSoldeEcus(montantTotal);
    }

    @Transactional
    public void ajouterScore(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter le score: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le score a ajouter doit etre positif");
        }

        int montantTotal = ferme.getScore() + montant;
        ferme.setScore(montantTotal);
    }

    @Transactional
    public Ferme acheterAnimal(Integer idFerme, String typeAnimal) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'acheter un animal: la ferme n'existe pas"));

        if (typeAnimal == null || typeAnimal.isBlank()) {
            throw new IllegalArgumentException("Type d'animal invalide");
        }

        String normalizedType = typeAnimal.trim().toLowerCase();
        int prix;

        switch (normalizedType) {
            case "vache":
                prix = 50;
                ferme.setNbVaches((ferme.getNbVaches() == null ? 0 : ferme.getNbVaches()) + 1);
                break;
            case "poule":
                prix = 10;
                ferme.setNbPoules((ferme.getNbPoules() == null ? 0 : ferme.getNbPoules()) + 1);
                break;
            case "lapin":
                prix = 10;
                ferme.setNbLapins((ferme.getNbLapins() == null ? 0 : ferme.getNbLapins()) + 1);
                break;
            default:
                throw new IllegalArgumentException("Type d'animal inconnu");
        }

        if (ferme.getSoldeEcus() < prix) {
            throw new IllegalArgumentException("Solde insuffisant");
        }

        ferme.setSoldeEcus(ferme.getSoldeEcus() - prix);
        return fermeRepository.save(ferme);
    }

    @Transactional
    public void hibernation(Integer idFerme, boolean bool) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible de modifier l'etat d'hibernation: la ferme n'existe pas"));

        ferme.setHibernation(bool);
    }

    @Transactional
    public void mettreAJourferme(Ferme ferme) {
        Integer fermeId = ferme.getIdFerme();
        long joursAbsence = ChronoUnit.DAYS.between(ferme.getDerniereCo(), LocalDateTime.now());

        if (joursAbsence <= 0) {
            return;
        }

        if (joursAbsence >= 3) {
            hibernation(fermeId, true);
        }

        if (joursAbsence >= 50) {
            deleteById(fermeId);
        }

        ferme.setDerniereCo(LocalDateTime.now());
        fermeRepository.save(ferme);
    }

    public Ferme getById(Integer id) {
        return fermeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
    }

    @Transactional
    public Ferme mettreAJourTempsEtPonte(Integer idFerme) {
        Ferme ferme = getById(idFerme);
        Remise remise = remiseRepository.findById(idFerme).orElseGet(() -> {
            Remise nouvelleRemise = new Remise();
            nouvelleRemise.setFerme(ferme);
            return remiseRepository.save(nouvelleRemise);
        });

        LocalDateTime maintenant = LocalDateTime.now();
        if (ferme.getDernierePonteOeufs() == null) {
            ferme.setDernierePonteOeufs(maintenant);
            return fermeRepository.save(ferme);
        }

        long secondesEcoulees = Duration.between(ferme.getDernierePonteOeufs(), maintenant).getSeconds();
        long joursEcoules = secondesEcoulees / GAME_DAY_DURATION_SECONDS;

        if (joursEcoules <= 0) {
            return ferme;
        }

        int nbPoules = ferme.getNbPoules() == null ? 0 : ferme.getNbPoules();
        int oeufsAjoutes = Math.toIntExact(joursEcoules * nbPoules);
        remise.setStockOeuf(remise.getStockOeuf() + oeufsAjoutes);
        ferme.setDernierePonteOeufs(
            ferme.getDernierePonteOeufs().plusSeconds(joursEcoules * GAME_DAY_DURATION_SECONDS)
        );

        remiseRepository.save(remise);
        return fermeRepository.save(ferme);
    }

    public List<Map<String, Object>> getClassementData() {
        return fermeRepository.findAll().stream()
            .sorted(
                Comparator.comparing((Ferme ferme) -> ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus())
                    .reversed()
                    .thenComparing(ferme -> ferme.getNom() == null ? "" : ferme.getNom())
            )
            .map(ferme -> Map.<String, Object>of(
                "name", ferme.getUtilisateur() != null && ferme.getUtilisateur().getGithubUsername() != null
                    ? ferme.getUtilisateur().getGithubUsername()
                    : (ferme.getNom() == null ? "-" : ferme.getNom()),
                "money", ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus(),
                "poules", ferme.getNbPoules() == null ? 0 : ferme.getNbPoules(),
                "vaches", ferme.getNbVaches() == null ? 0 : ferme.getNbVaches(),
                "lapins", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins()
            ))
            .toList();
    }
}
