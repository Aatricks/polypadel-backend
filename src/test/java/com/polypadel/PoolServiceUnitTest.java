package com.polypadel;

import com.polypadel.dto.PoolRequest;
import com.polypadel.dto.PoolResponse;
import com.polypadel.dto.TeamResponse;
import com.polypadel.model.Match;
import com.polypadel.model.Pool;
import com.polypadel.model.Team;
import com.polypadel.repository.MatchRepository;
import com.polypadel.repository.PoolRepository;
import com.polypadel.repository.TeamRepository;
import com.polypadel.service.PoolService;
import com.polypadel.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class PoolServiceUnitTest {

    @Mock private PoolRepository poolRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private TeamService teamService;
    @InjectMocks private PoolService poolService;

    private Team createTeam(long id) {
        Team t = new Team();
        t.setId(id);
        t.setCompany("Team" + id);
        return t;
    }

    @Test
    void createPoolAssignsTeams() {
        List<Long> teamIds = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        PoolRequest request = new PoolRequest("PoolA", teamIds);

        Pool savedPool = new Pool();
        savedPool.setId(10L);
        savedPool.setName("PoolA");

        when(poolRepository.existsByName("PoolA")).thenReturn(false);
        when(poolRepository.save(any(Pool.class))).thenReturn(savedPool);
        when(poolRepository.findById(10L)).thenReturn(Optional.of(savedPool));
        when(teamRepository.findByPoolId(10L)).thenReturn(teamIds.stream().map(this::createTeam).toList());
        when(teamService.toResponse(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            return new TeamResponse(team.getId(), team.getCompany(), List.of(), new TeamResponse.PoolInfo(10L, "PoolA"));
        });
        for (Long id : teamIds) {
            when(teamRepository.findById(id)).thenReturn(Optional.of(createTeam(id)));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        PoolResponse response = poolService.create(request);

        assertEquals("PoolA", response.name());
        assertEquals(6, response.teamsCount());
        verify(poolRepository).save(any(Pool.class));
    }

    @Test
    void createPoolValidations() {
        when(poolRepository.existsByName("Dup")).thenReturn(true);
        assertThrows(ResponseStatusException.class,
            () -> poolService.create(new PoolRequest("Dup", List.of(1L,2L,3L,4L,5L,6L))));

        assertThrows(ResponseStatusException.class,
            () -> poolService.create(new PoolRequest("BadSize", List.of(1L,2L))));

        when(poolRepository.existsByName("PoolB")).thenReturn(false);
        when(poolRepository.save(any(Pool.class))).thenAnswer(invocation -> {
            Pool p = invocation.getArgument(0);
            p.setId(20L);
            return p;
        });
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
            () -> poolService.create(new PoolRequest("PoolB", List.of(1L,2L,3L,4L,5L,6L)))).getStatusCode());
    }

    @Test
    void updateAndDeleteGuardAgainstPlayedMatches() {
        Pool pool = new Pool();
        pool.setId(30L);
        Team team = createTeam(100L);
        pool.setTeams(new ArrayList<>(List.of(team)));

        when(poolRepository.findById(30L)).thenReturn(Optional.of(pool));
        when(matchRepository.findCompletedByTeamId(100L)).thenReturn(List.of(new Match()));

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
            () -> poolService.update(30L, new PoolRequest("NewName", List.of()))).getStatusCode());

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
            () -> poolService.delete(30L)).getStatusCode());
    }

    @Test
    void deleteRemovesPoolWhenAllowed() {
        Pool pool = new Pool();
        pool.setId(40L);
        Team team = createTeam(200L);
        pool.setTeams(new ArrayList<>(List.of(team)));

        when(poolRepository.findById(40L)).thenReturn(Optional.of(pool));
        when(matchRepository.findCompletedByTeamId(anyLong())).thenReturn(List.of());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        poolService.delete(40L);

        assertNull(team.getPool());
        verify(poolRepository).delete(pool);
    }
}
