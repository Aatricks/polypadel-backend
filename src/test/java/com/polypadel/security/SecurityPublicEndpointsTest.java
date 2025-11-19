package com.polypadel.security;

import com.polypadel.events.service.EventService;
import com.polypadel.events.web.EventController;
import com.polypadel.rankings.dto.RankingRow;
import com.polypadel.rankings.service.RankingService;
import com.polypadel.rankings.web.RankingController;
import com.polypadel.security.handlers.RestAccessDeniedHandler;
import com.polypadel.security.handlers.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RankingController.class, EventController.class})
@Import({SecurityConfig.class, SecurityPublicEndpointsTest.TestBeans.class})
public class SecurityPublicEndpointsTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void ranking_is_public() throws Exception {
        UUID pouleId = UUID.randomUUID();
        mockMvc.perform(get("/rankings/poule/" + pouleId))
                .andExpect(status().isOk());
    }

    @Test
    void admin_requires_auth() throws Exception {
        // No auth -> should be 401 Unauthorized by SecurityConfig
        mockMvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateDebut\":\"2025-01-01\",\"dateFin\":\"2025-01-02\"}"))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class TestBeans {
        // Provide a no-op JWT filter to satisfy SecurityConfig without pulling full stack
        @Bean
        @Primary
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(null, null) {
                @Override
                protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                                jakarta.servlet.http.HttpServletResponse response,
                                                jakarta.servlet.FilterChain filterChain)
                        throws jakarta.servlet.ServletException, java.io.IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }

        // Bring in entry point/denied handlers used by SecurityConfig
        @Bean
        RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
            return new RestAuthenticationEntryPoint();
        }

        @Bean
        RestAccessDeniedHandler restAccessDeniedHandler() {
            return new RestAccessDeniedHandler();
        }

        // Stub RankingService to avoid Mockito/ByteBuddy on JDK 25
        @Bean
            @Primary
            RankingService rankingService() {
            return new RankingService(null, null) {
                @Override
                public java.util.List<RankingRow> rankingForPoule(java.util.UUID pouleId) {
                    return java.util.List.of(new RankingRow(java.util.UUID.randomUUID(), "", 0, 0, 0, 0, 0, 0, 0));
                }
            };
        }

        // Provide a minimal EventService bean so EventController can be created; methods won't be hit in these tests
        @Bean
            @Primary
            EventService eventService() {
            return new EventService(null, null, null) { };
        }
    }
}
