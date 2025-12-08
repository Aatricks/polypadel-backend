package com.polypadel.config;

import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.events.repository.EventRepository;
import com.polypadel.joueurs.repository.JoueurRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.poules.repository.PouleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializerTest {
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private PouleRepository pouleRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private com.polypadel.users.repository.UtilisateurRepository utilisateurRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    public void run_skips_when_players_exist() throws Exception {
        when(joueurRepository.count()).thenReturn(1L);
        dataInitializer.run();
        verify(joueurRepository, never()).saveAll(any());
    }

    @Test
    public void run_seeds_dev_data() throws Exception {
        when(joueurRepository.count()).thenReturn(0L);
        dataInitializer.run();
        verify(joueurRepository).saveAll(any());
        verify(pouleRepository).saveAll(any());
        verify(equipeRepository).saveAll(any());
        verify(eventRepository).save(any());
        verify(matchRepository).saveAll(any());
    }
}
