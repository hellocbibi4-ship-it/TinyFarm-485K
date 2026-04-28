/*
 * Interface Spring Data JPA pour la persistance des entités utilisateur de TinyFarm.
 */



package com.farm.tinyfarm.repository;

import com.farm.tinyfarm.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    
    Optional<Utilisateur> findByGithubUsername(String githubUsername);
    List<Utilisateur> findAllByGithubUsernameStartingWith(String prefix);
    void deleteByGithubUsername(String githubUsername);

}
