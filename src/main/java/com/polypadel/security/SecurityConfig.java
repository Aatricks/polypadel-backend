package com.polypadel.security;

import com.polypadel.security.handlers.RestAccessDeniedHandler;
import com.polypadel.security.handlers.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler accessDenied;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler accessDenied) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.accessDenied = accessDenied;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDenied))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/events/**", "/rankings/**").permitAll();
                auth.requestMatchers("/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/profile/**", "/results/**", "/matches/upcoming").hasAnyRole("JOUEUR", "ADMIN");
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
