package com.polypadel.joueurs.repository;

import com.polypadel.domain.entity.Joueur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JoueurRepository extends JpaRepository<Joueur, UUID> {
    Optional<Joueur> findByNumLicence(String numLicence);
    Optional<Joueur> findByUtilisateurId(UUID utilisateurId);

    @Query("select j from Joueur j where lower(j.nom) like lower(concat('%', :q, '%')) or lower(j.prenom) like lower(concat('%', :q, '%')) or lower(j.numLicence) like lower(concat('%', :q, '%'))")
    Page<Joueur> search(String q, Pageable pageable);
}
