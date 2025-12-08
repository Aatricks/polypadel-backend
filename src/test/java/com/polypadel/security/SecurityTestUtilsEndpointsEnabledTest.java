package com.polypadel.security;

import com.polypadel.test.TestUtilsController;
import com.polypadel.users.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.polypadel.security.handlers.RestAccessDeniedHandler;
import com.polypadel.security.handlers.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TestUtilsController.class})
@ActiveProfiles("dev")
@TestPropertySource(properties = "app.test.enabled=true")
@Import({SecurityConfig.class, SecurityTestUtilsEndpointsEnabledTest.TestBeans.class})
public class SecurityTestUtilsEndpointsEnabledTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UtilisateurRepository utilisateurRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Test
    void test_utils_is_allowed_when_enabled() throws Exception {
        mockMvc.perform(post("/test/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"e2e@example.com\", \"password\": \"Test123!\"}"))
                .andExpect(status().isOk());
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

        @Bean
        RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
            return new RestAuthenticationEntryPoint();
        }

        @Bean
        RestAccessDeniedHandler restAccessDeniedHandler() {
            return new RestAccessDeniedHandler();
        }
    }
}
