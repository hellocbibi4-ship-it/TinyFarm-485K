package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.repository.AnimalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Service gérant toutes les opérations liées aux vaches.
 *
 * Coûts des opérations (section 1.5 du cahier des charges) :
 *  - Nourrir  : 5 écus
 *  - Abreuver : 2 écus
 *  - Nettoyer : 3 écus
 *  - Soigner  : 6 écus
 *
 * Gains de poids :
 *  - Herbe  : +5 kg
 *  - Paille : +3 kg
 *  - Eau    : +1 kg (mais seule, ne fait pas grossir)
 *
 * Poids initial : 1 kg  |  Poids maximal : 750 kg
 * Passage adulte : âge >= 10 jours ET poids >= 80 kg
 *
 * Production de lait (adulte uniquement) :
 *  - 4L pour la 1ère traite de la journée
 *  - 8L pour la 2ème traite de la journée
 *  - 0L si sale OU malade OU n'a pas mangé
 */
@Service
public class VacheService {

    // ── Coûts ──────────────────────────────────────────────────────────────
    private static final int COUT_NOURRIR  = 5;
    private static final int COUT_ABREUVER = 2;
    private static final int COUT_NETTOYER = 3;
    private static final int COUT_SOIGNER  = 6;

    // ── Gains de poids ─────────────────────────────────────────────────────
    private static final float POIDS_HERBE  = 5f;
    private static final float POIDS_PAILLE = 3f;
    private static final float POIDS_EAU    = 1f; // uniquement si combinée à de la nourriture
    private static final float POIDS_MAX    = 750f;

    // ── Production de lait ─────────────────────────────────────────────────
    private static final int LITRES_PREMIERE_TRAITE  = 4;
    private static final int LITRES_DEUXIEME_TRAITE  = 8;

    // ── Seuils passage adulte ──────────────────────────────────────────────
    private static final int   AGE_ADULTE   = 10;  // jours
    private static final float POIDS_ADULTE = 80f; // kg

    private final AnimalRepository animalRepository;
    private final FermeService     fermeService;

    public VacheService(AnimalRepository animalRepository, FermeService fermeService) {
        this.animalRepository = animalRepository;
        this.fermeService     = fermeService;
    }

    // ── Assertion privée ───────────────────────────────────────────────────

    private void assertVache(Animal animal) {
        if (!TypeAnimal.VACHE.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas une vache.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Nourrir avec de l'herbe
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Nourrit la vache avec de l'herbe (+5 kg).
     * L'herbe pousse toute seule dans les pâturages.
     * - Coût : 5 écus.
     *
     * @throws IllegalStateException si la vache a déjà mangé aujourd'hui.
     */
    @Transactional
    public void nourrirHerbe(Animal animal) {
        assertVache(animal);

        if (animal.isAMange()) {
            throw new IllegalStateException(
                "ERREUR : La vache ne peut manger qu'une fois par jour.");
        }

        ajouterPoids(animal, POIDS_HERBE);
        animal.setJaugeFaim(100);
        animal.setAMange(true);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NOURRIR);
        animalRepository.save(animal);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Nourrir avec de la paille
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Nourrit la vache avec de la paille (+3 kg).
     * La paille s'achète à la coopérative (botte de paille).
     * - Coût : 5 écus.
     *
     * @throws IllegalStateException si la vache a déjà mangé aujourd'hui.
     */
    @Transactional
    public void nourrirPaille(Animal animal) {
        assertVache(animal);

        if (animal.isAMange()) {
            throw new IllegalStateException(
                "ERREUR : La vache ne peut manger qu'une fois par jour.");
        }

        ajouterPoids(animal, POIDS_PAILLE);
        animal.setJaugeFaim(100);
        animal.setAMange(true);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NOURRIR);
        animalRepository.save(animal);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Abreuver
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abreuve la vache.
     * - Remonte la jauge d'hydratation à 100.
     * - +1 kg seulement si la vache a aussi mangé dans la journée (seule, l'eau ne fait pas grossir).
     * - Coût : 2 écus.
     */
    @Transactional
    public void abreuver(Animal animal) {
        assertVache(animal);

        if (animal.getJaugeHydratation() == 100) {
            throw new IllegalStateException(
                "ERREUR : La vache est déjà pleinement hydratée.");
        }

        animal.setJaugeHydratation(100);

        // L'eau seule ne fait pas grossir, seulement combinée à de la nourriture
        if (animal.isAMange()) {
            ajouterPoids(animal, POIDS_EAU);
        }

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_ABREUVER);
        animalRepository.save(animal);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Nettoyer
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Nettoie la vache.
     * - Remonte la jauge de propreté à 100.
     * - Coût : 3 écus.
     */
    @Transactional
    public void nettoyer(Animal animal) {
        assertVache(animal);

        if (animal.getJaugeProprete() == 100) {
            throw new IllegalStateException(
                "ERREUR : La vache est déjà propre.");
        }

        animal.setJaugeProprete(100);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NETTOYER);
        animalRepository.save(animal);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Soigner
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Soigne la vache si elle est malade.
     * - Remonte la jauge de santé à 100 et la marque comme saine.
     * - Coût : 6 écus.
     *
     * @throws IllegalStateException si la vache n'est pas malade.
     */
    @Transactional
    public void soigner(Animal animal) {
        assertVache(animal);

        if (!animal.estMalade()) {
            throw new IllegalStateException(
                "ERREUR : La vache ne peut pas être soignée si elle n'est pas malade.");
        }

        animal.setEstMalade(false);
        animal.setJaugeSante(100);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_SOIGNER);
        animalRepository.save(animal);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Traire
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Trait la vache et retourne le nombre de litres produits.
     *
     * Conditions de production :
     *  - La vache doit être adulte.
     *  - La vache doit avoir mangé dans la journée.
     *  - La vache ne doit pas être sale ni malade.
     *
     * Production :
     *  - 4L pour la 1ère traite de la journée.
     *  - 8L pour la 2ème traite (toutes les 12h : 6h et 18h).
     *  - 0L si les conditions ne sont pas remplies.
     *
     * @return le nombre de litres de lait produits.
     */
    @Transactional
    public int traire(Animal animal) {
        assertVache(animal);

        // Conditions bloquantes
        if (!TypeStade.ADULTE.equals(animal.getStade())) {
            return 0; // Un veau ne produit pas de lait
        }
        if (!animal.isAMange()) {
            return 0; // La vache doit avoir mangé pour produire
        }
        if (animal.getJaugeProprete() < 100 || animal.estMalade()) {
            return 0; // Sale ou malade → pas de lait
        }

        int litres;
        if (!animal.isAEteTraite()) {
            litres = LITRES_PREMIERE_TRAITE;
            animal.setAEteTraite(true);
        } else {
            litres = LITRES_DEUXIEME_TRAITE;
        }

        animalRepository.save(animal);
        return litres;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Poids
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Augmente le poids de la vache dans la limite de {@value #POIDS_MAX} kg.
     *
     * @param animal    la vache concernée.
     * @param addWeight le poids à ajouter (en kg).
     */
    public void ajouterPoids(Animal animal, float addWeight) {
        assertVache(animal);

        float nouveauPoids = Math.min(animal.getPoids() + addWeight, POIDS_MAX);
        animal.setPoids(nouveauPoids);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Mise à jour quotidienne
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Met à jour la vache en fin de journée :
     * - Incrémente l'âge d'un jour.
     * - Passe la vache au stade adulte si âge >= 10 jours ET poids >= 80 kg.
     * - Réinitialise {@code aMange} et {@code aEteTraite} pour la prochaine journée.
     */
    @Transactional
    public void mettreAJour(Animal animal) {
        assertVache(animal);

        animal.setAge(animal.getAge() + 1);

        if (TypeStade.ENFANT.equals(animal.getStade())
                && animal.getAge() >= AGE_ADULTE
                && animal.getPoids() >= POIDS_ADULTE) {
            animal.setStade(TypeStade.ADULTE);
        }

        // Réinitialisations journalières
        animal.setAMange(false);
        animal.setAEteTraite(false);

        animalRepository.save(animal);
    }
}