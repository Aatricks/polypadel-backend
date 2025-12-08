package com.polypadel;

import com.polypadel.dto.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void loginRequest() {
        LoginRequest req = new LoginRequest("test@test.com", "password");
        assertEquals("test@test.com", req.email());
        assertEquals("password", req.password());
    }

    @Test
    void loginResponse() {
        LoginResponse.UserDto user = new LoginResponse.UserDto(1L, "test@test.com", "JOUEUR", false);
        LoginResponse resp = new LoginResponse("token", "bearer", user);
        
        assertEquals("token", resp.accessToken());
        assertEquals("bearer", resp.tokenType());
        assertEquals(1L, resp.user().id());
        assertEquals("test@test.com", resp.user().email());
        assertFalse(resp.user().mustChangePassword());
    }

    @Test
    void playerRequest() {
        PlayerRequest req = new PlayerRequest("John", "Doe", "Corp", "L123456", "john@test.com");
        assertEquals("John", req.firstName());
        assertEquals("Doe", req.lastName());
        assertEquals("Corp", req.company());
        assertEquals("L123456", req.licenseNumber());
        assertEquals("john@test.com", req.email());
    }

    @Test
    void playerResponse() {
        PlayerResponse resp = new PlayerResponse(1L, "John", "Doe", "Corp", "L123456", LocalDate.of(1990, 1, 1), "/photo.jpg", true);
        assertEquals(1L, resp.id());
        assertEquals("John", resp.firstName());
        assertEquals("Doe", resp.lastName());
        assertEquals("Corp", resp.company());
        assertEquals("L123456", resp.licenseNumber());
        assertEquals(LocalDate.of(1990, 1, 1), resp.birthDate());
        assertEquals("/photo.jpg", resp.photoUrl());
        assertTrue(resp.hasAccount());
    }

    @Test
    void teamRequest() {
        TeamRequest req = new TeamRequest("Corp", 1L, 2L, 3L);
        assertEquals("Corp", req.company());
        assertEquals(1L, req.player1Id());
        assertEquals(2L, req.player2Id());
        assertEquals(3L, req.poolId());
    }

    @Test
    void teamResponse() {
        TeamResponse.PlayerInfo p1 = new TeamResponse.PlayerInfo(1L, "John", "Doe");
        TeamResponse.PlayerInfo p2 = new TeamResponse.PlayerInfo(2L, "Jane", "Doe");
        TeamResponse.PoolInfo pool = new TeamResponse.PoolInfo(1L, "Poule A");
        TeamResponse resp = new TeamResponse(1L, "Corp", List.of(p1, p2), pool);
        
        assertEquals(1L, resp.id());
        assertEquals("Corp", resp.company());
        assertEquals(2, resp.players().size());
        assertEquals("Poule A", resp.pool().name());
    }

    @Test
    void poolRequest() {
        PoolRequest req = new PoolRequest("Poule A", List.of(1L, 2L, 3L, 4L, 5L, 6L));
        assertEquals("Poule A", req.name());
        assertEquals(6, req.teamIds().size());
    }

    @Test
    void poolResponse() {
        PoolResponse resp = new PoolResponse(1L, "Poule A", 6, List.of());
        assertEquals(1L, resp.id());
        assertEquals("Poule A", resp.name());
        assertEquals(6, resp.teamsCount());
    }

    @Test
    void eventRequest() {
        EventRequest.MatchInfo m1 = new EventRequest.MatchInfo(1, 1L, 2L);
        EventRequest req = new EventRequest(LocalDate.of(2025, 12, 15), LocalTime.of(19, 30), List.of(m1));
        
        assertEquals(LocalDate.of(2025, 12, 15), req.eventDate());
        assertEquals(LocalTime.of(19, 30), req.eventTime());
        assertEquals(1, req.matches().size());
        assertEquals(1, req.matches().get(0).courtNumber());
    }

    @Test
    void eventResponse() {
        EventResponse resp = new EventResponse(1L, LocalDate.of(2025, 12, 15), LocalTime.of(19, 30), List.of());
        assertEquals(1L, resp.id());
        assertEquals(LocalDate.of(2025, 12, 15), resp.eventDate());
    }

    @Test
    void matchResponse() {
        MatchResponse.EventInfo event = new MatchResponse.EventInfo(LocalDate.of(2025, 12, 15), LocalTime.of(19, 30));
        MatchResponse resp = new MatchResponse(1L, event, 1, null, null, "A_VENIR", null, null);
        
        assertEquals(1L, resp.id());
        assertEquals(1, resp.courtNumber());
        assertEquals("A_VENIR", resp.status());
    }

    @Test
    void matchUpdateRequest() {
        MatchUpdateRequest req = new MatchUpdateRequest("TERMINE", "6-4, 6-3", "4-6, 3-6");
        assertEquals("TERMINE", req.status());
        assertEquals("6-4, 6-3", req.scoreTeam1());
        assertEquals("4-6, 3-6", req.scoreTeam2());
    }

    @Test
    void rankingRow() {
        RankingRow row = new RankingRow(1, "Corp", 10, 7, 3, 21, 16, 8);
        assertEquals(1, row.position());
        assertEquals("Corp", row.company());
        assertEquals(10, row.matchesPlayed());
        assertEquals(7, row.wins());
        assertEquals(3, row.losses());
        assertEquals(21, row.points());
    }

    @Test
    void passwordChangeRequest() {
        PasswordChangeRequest req = new PasswordChangeRequest("old", "new", "new");
        assertEquals("old", req.currentPassword());
        assertEquals("new", req.newPassword());
        assertEquals("new", req.confirmPassword());
    }

    @Test
    void profileResponse() {
        ProfileResponse.UserInfo user = new ProfileResponse.UserInfo(1L, "test@test.com", "JOUEUR");
        ProfileResponse resp = new ProfileResponse(user, null);
        assertEquals(1L, resp.user().id());
        assertEquals("test@test.com", resp.user().email());
    }

    @Test
    void profileUpdateRequest() {
        ProfileUpdateRequest req = new ProfileUpdateRequest("John", "Doe", LocalDate.of(1990, 1, 1), "john@test.com");
        assertEquals("John", req.firstName());
        assertEquals("Doe", req.lastName());
        assertEquals(LocalDate.of(1990, 1, 1), req.birthDate());
        assertEquals("john@test.com", req.email());
    }
}
