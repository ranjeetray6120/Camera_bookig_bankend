package com.example.camerabooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.info("Configuring security...");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())  // Disable CSRF (use with caution)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/bookings/**").permitAll()  
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/ws/**").permitAll()  // Allow WebSocket endpoint
                .requestMatchers("/viewer.html", "/static/**", "/public/**", "/resources/**").permitAll() // Allow static resources
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow Swagger UI if used
                .requestMatchers("/api/**").permitAll() // Allow all API calls (if necessary)
                .requestMatchers("/actuator/**").permitAll() // Allow Spring Actuator for monitoring
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "http://127.0.0.1:5500",
            "https://camerabooking.netlify.app",
            "http://localhost:4200",
            "http://localhost:8080/viewer.html"
            "https://camerabookingweb.netlify.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization", "Access-Control-Allow-Origin"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        LOGGER.info("CORS Configuration Applied Successfully.");
        
        return source;
    }
}
