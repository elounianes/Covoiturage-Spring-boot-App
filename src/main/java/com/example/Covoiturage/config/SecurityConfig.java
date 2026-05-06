// NOTE THIS FILE IS AI GENERATED 


package com.example.Covoiturage.config;

import java.util.HashMap;
import java.util.Map;

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

import com.example.Covoiturage.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration // tells Spring Boot that this is a Configuration
@EnableWebSecurity // enables web security and tells spring to handle the URL security and dont go with the default Spring Security
@EnableMethodSecurity //lets you use @PreAuthorize("hasRole('ADMIN')")
public class SecurityConfig {
    // to go back for
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // BCrypt is the industry standard for password hashing.
    // This bean is used in two places:
    //   1. AuthServiceImpl.creerCompte()  → encode the password before saving
    //   2. DaoAuthenticationProvider     → verify password on login
    // Declaring it as a @Bean means Spring injects it wherever needed.
    @Bean // Spring handles the life Cycle of this methode and lets spring boot know to inject it wherever needed !! not handles manually like sevices and repositories 
    public PasswordEncoder passwordEncoder() { // passwordEncoder is a bean that is used to encode and decode passwords
        return new BCryptPasswordEncoder(); // used to hash passwords and verify passwords during login!!

    }

    // DaoAuthenticationProvider is Spring's built-in connector between:
    //   - your UserDetailsService (loads the user from DB)
    //   - your PasswordEncoder   (verifies the password)
    // Without this, Spring doesn't know which UserDetailsService
 
    // or PasswordEncoder to use during login.
 
  
    @Bean
    public DaoAuthenticationProvider authenticationProvider() { //DaoAuthenticationProvider is a class that handles the authentication of users
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService); // create an authentication provider using the UserDetailsServiceImpl to load users from DB
    provider.setPasswordEncoder(passwordEncoder()); // set the password encoder to the authentication provider to encode and decode passwords
    return provider; // rturns the configured provider so Spring can use it for authentication
}

  



    // ── AuthenticationManager 
    // Exposed as a @Bean so AuthController can inject it
    // and call authenticate() directly when processing login requests.
    @Bean
    public AuthenticationManager authenticationManager( //This is the main component responsible for authentication
            AuthenticationConfiguration config) throws Exception { //  AuthenticationConfiguration is  a class that provides the configuration for the authentication manager
        return config.getAuthenticationManager(); // AuthenticationManager getAuthenticationManager() method returns the authentication manager 
    }

    //security filter chain 
    // how requests are secured and who can access what
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { // HttpSecurity is a class that provides the configuration for the security filter chain 

        http
            
            // CSRF protection is designed for browser-based form submissions
            //CSRF protection ensures that state-changing requests (POST, PUT, DELETE) come from a trusted source
            //  It usually works with a CSRF token sent in forms
           
            .csrf(AbstractHttpConfigurer::disable) //  building a REST API  use JSON requests (fetch / Postman), not browser forms

            // Allow H2 console iframes 
            // H2 console uses iframes; Spring Security blocks them by default.
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )

            // ── URL-based access rules 
            // Order matters — Spring checks rules top to bottom,
            // stops at the first match. Always put specific rules first.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers( // public front files
                "/",
                "/index.html",
                "/browse.html",
                "/passenger.html",
                "/driver.html",
                "/admin.html",
                "/notifications.html",
                "/css/**",
                "/js/**"
            ).permitAll()

                // Public endpoints — no login required
                .requestMatchers(
                    "/api/auth/**",   // register, login, logout
                    "/actuator/**", // test POSTMAN
                    "/api/trajets/disponibles", // browse page is public
                    "/h2-console/**"  // remove this in production
                ).permitAll()
                //Role-based access
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
            // how the login form works !! and handeled
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

                    // here the use of the hashmap to build  json response
                    //  json response will contain the user's email and role  !!
                    Map<String, Object> body = new HashMap<>();
                    body.put("success", true);
                    body.put("email", authentication.getName());
                    body.put("role", authentication.getAuthorities()
                        .iterator().next().getAuthority()); // get the role of the user 

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