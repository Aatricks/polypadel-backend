package com.polypadel.results.web;

import com.polypadel.matches.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResultsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ResultsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;

    @Test
    public void userResults_ok() throws Exception {
        when(matchService.finishedForCurrentUser()).thenReturn(List.of());
        mockMvc.perform(get("/results/user")).andExpect(status().isOk());
    }
}
