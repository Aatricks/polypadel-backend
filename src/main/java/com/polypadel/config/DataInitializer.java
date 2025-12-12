package com.polypadel.config;

import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User("admin@padel.com", passwordEncoder.encode("Admin@2025!"), Role.ADMINISTRATEUR);
            admin = userRepository.save(admin);

            // Create player user
            User playerUser = new User("joueur@padel.com", passwordEncoder.encode("Joueur@2025!"), Role.JOUEUR);
            playerUser = userRepository.save(playerUser);

            // Create sample players
            Player p1 = new Player();
            p1.setFirstName("Jean");
            p1.setLastName("Dupont");
            p1.setCompany("Tech Corp");
            p1.setLicenseNumber("L123456");
            p1.setUser(playerUser);
            playerRepository.save(p1);

            Player p2 = new Player();
            p2.setFirstName("Marie");
            p2.setLastName("Martin");
            p2.setCompany("Tech Corp");
            p2.setLicenseNumber("L123457");
            p2.setEmail("marie.martin@test.com");
            playerRepository.save(p2);

            Player p3 = new Player();
            p3.setFirstName("Pierre");
            p3.setLastName("Durand");
            p3.setCompany("Innov Ltd");
            p3.setLicenseNumber("L123458");
            p3.setEmail("pierre.durand@test.com");
            playerRepository.save(p3);

            Player p4 = new Player();
            p4.setFirstName("Sophie");
            p4.setLastName("Bernard");
            p4.setCompany("Innov Ltd");
            p4.setLicenseNumber("L123459");
            p4.setEmail("sophie.bernard@test.com");
            playerRepository.save(p4);

            System.out.println("=== Test accounts created ===");
            System.out.println("Admin: admin@padel.com / Admin@2025!");
            System.out.println("Player: joueur@padel.com / Joueur@2025!");
        }
    }
}
