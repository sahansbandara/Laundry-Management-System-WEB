package com.laundry.lms.config;

import com.laundry.lms.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/api/**"),
                new AntPathRequestMatcher("/h2-console/**")))
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
                "/api/auth/**",
                "/api/catalog/**",
                "/h2-console/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/",
                "/index.html",
                "/login.html",
                "/register.html",
                "/*.html",
                "/css/**",
                "/js/**",
                "/images/**",
                "/frontend/**")
            .permitAll()

            // Customer endpoints
            .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

            // Staff order endpoints
            .requestMatchers("/api/staff/orders/**").hasAnyRole("LAUNDRY_STAFF", "ADMIN")

            // Staff task endpoints
            .requestMatchers("/api/staff/tasks/**").hasAnyRole("LAUNDRY_STAFF", "DELIVERY_STAFF", "ADMIN")

            // Delivery endpoints
            .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY_STAFF", "ADMIN")

            // Finance endpoints
            .requestMatchers("/api/finance/**").hasAnyRole("FINANCE_STAFF", "ADMIN")

            // Customer service endpoints
            .requestMatchers("/api/customer-service/**").hasAnyRole("CUSTOMER_SERVICE", "ADMIN")

            // Chat endpoints (shared)
            .requestMatchers("/api/chat/**").hasAnyRole("CUSTOMER", "CUSTOMER_SERVICE", "ADMIN")

            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Legacy endpoints (keep for backward compatibility)
            .requestMatchers("/api/orders/**", "/api/payments/**", "/api/tasks/**", "/api/messages/**")
            .permitAll()

            // All other requests require authentication
            .anyRequest().authenticated())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:8080", "http://localhost:8081"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
