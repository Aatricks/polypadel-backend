package com.polypadel.config;

import com.polypadel.domain.entity.*;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.domain.enums.Role;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.users.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.poules.repository.PouleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JoueurRepository joueurRepo;
    private final EquipeRepository equipeRepo;
    private final PouleRepository pouleRepo;
    private final EventRepository eventRepo;
    private final MatchRepository matchRepo;
    private final UtilisateurRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(JoueurRepository joueurRepo, EquipeRepository equipeRepo, PouleRepository pouleRepo,
                           EventRepository eventRepo, MatchRepository matchRepo, UtilisateurRepository userRepo,
                           PasswordEncoder encoder) {
        this.joueurRepo = joueurRepo;
        this.equipeRepo = equipeRepo;
        this.pouleRepo = pouleRepo;
        this.eventRepo = eventRepo;
        this.matchRepo = matchRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (joueurRepo.count() > 0) return;

        // Create 12 players
        String[] names = {"Alice","Bob","Clara","David","Eve","Frank","Grace","Hank","Ivy","Jack","Karen","Leo"};
        List<Joueur> joueurs = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Joueur j = new Joueur();
            j.setNom(names[i]);
            j.setPrenom(names[i]);
            j.setNumLicence(String.format("L%06d", i + 1));
            j.setEntreprise("PolyCorp");
            j.setDateNaissance(LocalDate.of(1990, (i % 12) + 1, (i % 28) + 1));
            joueurs.add(j);
        }
        joueurRepo.saveAll(joueurs);

        // Create poule
        Poule poule = new Poule();
        poule.setNom("Poule A");
        pouleRepo.save(poule);

        // Create 6 teams
        List<Equipe> teams = new ArrayList<>();
        for (int t = 0; t < 6; t++) {
            Equipe e = new Equipe();
            e.setEntreprise("PolyCorp");
            e.setJoueur1(joueurs.get(t * 2));
            e.setJoueur2(joueurs.get(t * 2 + 1));
            e.setPoule(poule);
            teams.add(e);
        }
        equipeRepo.saveAll(teams);

        // Create event with matches
        Evenement event = new Evenement();
        event.setDateDebut(LocalDate.now());
        event.setDateFin(LocalDate.now().plusDays(1));
        eventRepo.save(event);

        Match m = new Match();
        m.setEvenement(event);
        m.setEquipe1(teams.get(0));
        m.setEquipe2(teams.get(1));
        m.setPiste(1);
        m.setStartTime(LocalTime.of(9, 0));
        m.setStatut(MatchStatus.A_VENIR);
        matchRepo.save(m);

        // Create admin user
        if (userRepo.findByEmail("admin@padel.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setEmail("admin@padel.com");
            admin.setEmailHash("admin");
            admin.setPasswordHash(encoder.encode("Admin@2025!"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepo.save(admin);
        }
    }
}
