package com.polypadel.config;

import com.polypadel.domain.entity.*;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.poules.repository.PouleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple development data seeding. Runs only for the "dev" profile.
 * Keeps logic minimal: if any Joueur rows already exist we assume data was seeded.
 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;
    private final PouleRepository pouleRepository;
    private final EventRepository eventRepository;
    private final MatchRepository matchRepository;

    public DataInitializer(JoueurRepository joueurRepository,
                           EquipeRepository equipeRepository,
                           PouleRepository pouleRepository,
                           EventRepository eventRepository,
                           MatchRepository matchRepository) {
        this.joueurRepository = joueurRepository;
        this.equipeRepository = equipeRepository;
        this.pouleRepository = pouleRepository;
        this.eventRepository = eventRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (joueurRepository.count() > 0) {
            log.info("Skipping dev data initialization: players already present.");
            return;
        }
        log.info("Seeding dev data (development profile)...");

        // -- Players --------------------------------------------------------
        // Create 12 distinct players (enables 6 valid teams in a poule)
        List<Joueur> joueurs = new ArrayList<>();
        String[] names = {"Alice","Bob","Clara","David","Eve","Frank","Grace","Hank","Ivy","Jack","Karen","Leo"};
        for (int i = 0; i < names.length; i++) {
            Joueur j = new Joueur();
            j.setNom(names[i]);
            j.setPrenom(names[i]); // keep prenom simple
            j.setNumLicence(String.format("LIC-%02d", i + 1));
            j.setEntreprise("PolyCorp");
            // simple deterministic DOB ensuring age > 16
            j.setDateNaissance(LocalDate.of(1990 + (i % 10), (i % 12) + 1, ((i % 27) + 1)));
            joueurs.add(j);
        }
        joueurRepository.saveAll(joueurs);

        // -- Poules ---------------------------------------------------------
        Poule p1 = new Poule(); p1.setNom("Poule 1");
        Poule p2 = new Poule(); p2.setNom("Poule 2");
        List<Poule> poules = new ArrayList<>();
        poules.add(p1); poules.add(p2);
        pouleRepository.saveAll(poules);

        // -- Teams (exactly 6 teams in Poule 1) -----------------------------
        List<Equipe> equipes = new ArrayList<>();
        for (int t = 0; t < 6; t++) {
            Equipe team = new Equipe();
            team.setEntreprise("PolyCorp");
            team.setJoueur1(joueurs.get(t * 2));
            team.setJoueur2(joueurs.get(t * 2 + 1));
            team.setPoule(p1); // satisfy poule size rule exactly 6 teams
            equipes.add(team);
        }
        equipeRepository.saveAll(equipes);

        // -- Event ---------------------------------------------------------
        Evenement event = new Evenement();
        event.setDateDebut(LocalDate.now());
        event.setDateFin(LocalDate.now().plusDays(1));
        eventRepository.save(event);

        // -- Matches (upcoming) --------------------------------------------
        Match m1 = new Match();
        m1.setEvenement(event);
        m1.setEquipe1(equipes.get(0));
        m1.setEquipe2(equipes.get(1));
        m1.setPiste(1);
        m1.setStartTime(LocalTime.of(9,0));
        m1.setStatut(MatchStatus.A_VENIR);

        Match m2 = new Match();
        m2.setEvenement(event);
        m2.setEquipe1(equipes.get(2));
        m2.setEquipe2(equipes.get(3));
        m2.setPiste(2);
        m2.setStartTime(LocalTime.of(10,30));
        m2.setStatut(MatchStatus.A_VENIR);

        List<Match> matches = new ArrayList<>();
        matches.add(m1); matches.add(m2);
        matchRepository.saveAll(matches);

        log.info("Dev data seeding complete. Players: {} Teams: {}", joueurs.size(), equipes.size());
    }
}
