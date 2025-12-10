package com.polypadel;

import com.polypadel.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void userModel() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPasswordHash("hash");
        user.setRole(Role.JOUEUR);
        user.setActive(true);
        user.setMustChangePassword(false);

        assertEquals(1L, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(Role.JOUEUR, user.getRole());
        assertTrue(user.isActive());
        assertFalse(user.isMustChangePassword());
    }

    @Test
    void userConstructor() {
        User user = new User("email@test.com", "hash", Role.ADMINISTRATEUR);
        assertEquals("email@test.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(Role.ADMINISTRATEUR, user.getRole());
    }

    @Test
    void playerModel() {
        Player player = new Player();
        player.setId(1L);
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setCompany("Corp");
        player.setLicenseNumber("L123456");
        player.setBirthDate(null);
        player.setPhotoUrl("/photo.jpg");
        player.setUser(null);

        assertEquals(1L, player.getId());
        assertEquals("John", player.getFirstName());
        assertEquals("Doe", player.getLastName());
        assertEquals("Corp", player.getCompany());
        assertEquals("L123456", player.getLicenseNumber());
        assertNull(player.getBirthDate());
        assertEquals("/photo.jpg", player.getPhotoUrl());
        assertNull(player.getUser());
    }

    @Test
    void teamModel() {
        Team team = new Team();
        team.setId(1L);
        team.setCompany("Corp");
        
        Player p1 = new Player();
        p1.setId(1L);
        Player p2 = new Player();
        p2.setId(2L);
        
        team.setPlayer1(p1);
        team.setPlayer2(p2);
        team.setPool(null);

        assertEquals(1L, team.getId());
        assertEquals("Corp", team.getCompany());
        assertEquals(1L, team.getPlayer1().getId());
        assertEquals(2L, team.getPlayer2().getId());
        assertNull(team.getPool());
    }

    @Test
    void poolModel() {
        Pool pool = new Pool();
        pool.setId(1L);
        pool.setName("Poule A");

        assertEquals(1L, pool.getId());
        assertEquals("Poule A", pool.getName());
        assertNotNull(pool.getTeams());
    }

    @Test
    void eventModel() {
        Event event = new Event();
        event.setId(1L);
        event.setEventDate(java.time.LocalDate.of(2025, 12, 15));
        event.setEventTime(java.time.LocalTime.of(19, 30));

        assertEquals(1L, event.getId());
        assertEquals(java.time.LocalDate.of(2025, 12, 15), event.getEventDate());
        assertEquals(java.time.LocalTime.of(19, 30), event.getEventTime());
        assertNotNull(event.getMatches());
    }

    @Test
    void matchModel() {
        Match match = new Match();
        match.setId(1L);
        match.setCourtNumber(5);
        match.setStatus(MatchStatus.A_VENIR);
        match.setScoreTeam1("6-4, 6-3");
        match.setScoreTeam2("4-6, 3-6");

        assertEquals(1L, match.getId());
        assertEquals(5, match.getCourtNumber());
        assertEquals(MatchStatus.A_VENIR, match.getStatus());
        assertEquals("6-4, 6-3", match.getScoreTeam1());
        assertEquals("4-6, 3-6", match.getScoreTeam2());
    }

    @Test
    void loginAttemptModel() {
        LoginAttempt attempt = new LoginAttempt("test@test.com");
        attempt.setAttemptsCount(3);
        attempt.setLastAttempt(java.time.LocalDateTime.now());
        attempt.setLockedUntil(null);

        assertEquals("test@test.com", attempt.getEmail());
        assertEquals(3, attempt.getAttemptsCount());
        assertNotNull(attempt.getLastAttempt());
        assertNull(attempt.getLockedUntil());
    }

    @Test
    void enumValues() {
        assertEquals(2, Role.values().length);
        assertEquals(Role.JOUEUR, Role.valueOf("JOUEUR"));
        assertEquals(Role.ADMINISTRATEUR, Role.valueOf("ADMINISTRATEUR"));

        assertEquals(3, MatchStatus.values().length);
        assertEquals(MatchStatus.A_VENIR, MatchStatus.valueOf("A_VENIR"));
        assertEquals(MatchStatus.TERMINE, MatchStatus.valueOf("TERMINE"));
        assertEquals(MatchStatus.ANNULE, MatchStatus.valueOf("ANNULE"));
    }
}
