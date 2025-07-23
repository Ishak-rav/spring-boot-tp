package com.example.config;

import com.example.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité Spring Security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration du filtre de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/unresolved").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/public").permitAll()

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/tickets").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/user/{userId}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/search").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/priorites/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/tickets/{id}/resolve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/{id}/reopen").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/{id}").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/priorites").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/priorites/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/priorites/{id}").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/{id}").hasAuthority("ADMIN")

                        .requestMatchers("/api/utilisateurs/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}