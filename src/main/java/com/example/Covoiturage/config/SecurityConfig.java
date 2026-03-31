package com.example.Covoiturage.config;

import com.example.Covoiturage.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity lets you use @PreAuthorize("hasRole('ADMIN')")
// directly on service or controller methods — cleaner than hardcoding URLs
@EnableMethodSecurity
public class SecurityConfig {

    // Spring will inject this — it is defined in UserDetailsServiceImpl
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ── PasswordEncoder ───────────────────────────────────
    // BCrypt is the industry standard for password hashing.
    // This bean is used in two places:
    //   1. AuthServiceImpl.creerCompte()  → encode the password before saving
    //   2. DaoAuthenticationProvider     → verify password on login
    // Declaring it as a @Bean means Spring injects it wherever needed.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── AuthenticationProvider ────────────────────────────
    // DaoAuthenticationProvider is Spring's built-in connector between:
    //   - your UserDetailsService (loads the user from DB)
    //   - your PasswordEncoder   (verifies the password)
    // Without this, Spring doesn't know which UserDetailsService
    // or PasswordEncoder to use during login.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}

    // ── AuthenticationManager ─────────────────────────────
    // Exposed as a @Bean so AuthController can inject it
    // and call authenticate() directly when processing login requests.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Main security filter chain ────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ── CSRF ─────────────────────────────────────
            // CSRF protection is designed for browser-based form submissions
            // where a malicious site tricks the browser into sending a request.
            // For REST APIs consumed by fetch() with JSON bodies, CSRF tokens
            // are not the right protection mechanism — we disable it here.
            // In production with real JWT tokens, you would handle this differently.
            .csrf(AbstractHttpConfigurer::disable)

            // ── URL-based access rules ────────────────────
            // Order matters — Spring checks rules top to bottom,
            // stops at the first match. Always put specific rules first.
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no login required
                .requestMatchers(
                    "/api/auth/**",   // register, login, logout
                    "/api/trajets/disponibles", // browse page is public
                    "/h2-console/**"  // remove this in production
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Driver-only endpoints
                .requestMatchers("/api/chauffeur/**").hasRole("CHAUFFEUR")

                // Passenger-only endpoints
                .requestMatchers("/api/passager/**").hasRole("PASSAGER")

                // Everything else requires any authenticated user
                .anyRequest().authenticated()
            )

            // ── Login behavior ────────────────────────────
            // We override the default Spring login behavior completely.
            // Instead of redirecting to /login page (Thymeleaf behavior),
            // we return JSON — because fetch() cannot follow redirects usefully.
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")  // POST to this URL
                .usernameParameter("email")             // match your login JSON field
                .passwordParameter("password")

                // On success: return 200 + JSON with user info
                .successHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    // Build a response body with the user's role so the
                    // frontend knows which dashboard to redirect to
                    Map<String, Object> body = new HashMap<>();
                    body.put("success", true);
                    body.put("email", authentication.getName());
                    body.put("role", authentication.getAuthorities()
                        .iterator().next().getAuthority()); //  ROLE_CHAUFFEUR

                    new ObjectMapper().writeValue(response.getWriter(), body);
                })

                // On failure: return 401 + JSON error message
                // This covers wrong password AND blocked/suspended accounts
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> body = new HashMap<>();
                    body.put("success", false);
                    body.put("error", exception.getMessage());

                    new ObjectMapper().writeValue(response.getWriter(), body);
                })
            )

            // ── Logout behavior 
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")

                // On success: return 200 + JSON instead of redirecting
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"success\": true, " +
                        "\"message\": \"Déconnexion réussie\"}");
                })
                .invalidateHttpSession(true)   // destroy the server-side session
                .deleteCookies("JSESSIONID")   // tell browser to remove the cookie
            )

            // ── 401 for unauthenticated requests ──────────
            // By default Spring redirects to /login — useless for fetch().
            // This makes it return a clean 401 JSON instead.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        "{\"error\": \"Non authentifié — veuillez vous connecter\"}");
                })

                // 403 for authenticated users accessing wrong role's endpoint
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        "{\"error\": \"Accès refusé — permissions insuffisantes\"}");
                })
            )

            // Register the authentication provider we built above
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}