package com.farm.tinyfarm.repository;

import com.farm.tinyfarm.model.Ferme;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class FermeRepository {

    private final JdbcTemplate jdbcTemplate;

    //Constructeur du repository
    public FermeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Création d'une ferme
    public int createFerme(Ferme ferme) {
        String sql = """
            INSERT INTO ferme (nom, soldeEcus, hibernation, 
            score, idUtilisateur) VALUES (?, ?, ?, ?, ?) 
            """;
        return jdbcTemplate.update(sql, 
                ferme.getNom(),
                ferme.getSoldeEcus(),
                ferme.getHibernation(),
                ferme.getScore(),
                ferme.getIdUtilisateur()
        );
    }

    //Lecture de toutes les fermes de la base de données
    public List<Ferme> findAllFermes() {
        String sql = "SELECT * FROM ferme";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ferme f = new Ferme();
            f.setIdFerme(rs.getInt("idFerme"));
            f.setIdUtilisateur(rs.getInt("idUtilisateur"));
            f.setNom(rs.getString("nom"));
            f.setSoldeEcus(rs.getInt("soldeEcus"));
            f.setHibernation(rs.getBoolean("hibernation"));
            f.setScore(rs.getInt("score"));
            f.setDateCreation(rs.getTimestamp("dateCreation").toLocalDateTime());
            return f;
        });
    }

    //Lecture d'une ferme selon son id 
    public Ferme findFermeById(int id) {
        String sql = "SELECT * FROM ferme WHERE idFerme = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Ferme f = new Ferme();
            f.setIdFerme(id);
            f.setIdUtilisateur(rs.getInt("idUtilisateur"));
            f.setNom(rs.getString("nom"));
            f.setSoldeEcus(rs.getInt("soldeEcus"));
            f.setHibernation(rs.getBoolean("hibernation"));
            f.setScore(rs.getInt("score"));
            f.setDateCreation(rs.getTimestamp("dateCreation").toLocalDateTime());
            return f;
        });
    }

    //Update d'une ferme
    public int update(int id, Ferme ferme){
        String sql = """
            UPDATE ferme
            SET nom = ?, soldeEcus = ?, score = ?, hibernation = ?
            WHERE idFerme = id
            """;
        
        return jdbcTemplate.update(sql, 
            ferme.getNom(),
            ferme.getSoldeEcus(),
            ferme.getScore(),
            ferme.getHibernation()
        );
    }

    //Suppression d'une ferme
    public int delete(int id) {
        String sql = "DELETE FROM ferme WHERE idFerme = ?";
        return jdbcTemplate.update(sql, id);
    }
}
