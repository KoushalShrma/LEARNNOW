package me.learn.now.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import me.learn.now.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Hinglish: JWT ke saath stateless authentication setup kar rahe hai
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Hinglish: public endpoints - registration, login ke liye open
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // Hinglish: YouTube API endpoints ko public kar diya demo ke liye
                        .requestMatchers("/api/youtube/**").permitAll()
                        // Hinglish: Course endpoints ko public kar diya for AI subtopic generation
                        .requestMatchers("/api/courses/**").permitAll()
                        // Hinglish: Topics endpoints ko public kar diya
                        .requestMatchers("/api/topics/**").permitAll()
                        // Hinglish: Users endpoints ko public kar diya
                        .requestMatchers("/api/users/**").permitAll()
                        // Hinglish: Videos endpoints ko public kar diya
                        .requestMatchers("/api/videos/**").permitAll()
                        // Hinglish: Learning paths endpoints ko public kar diya
                        .requestMatchers("/api/learning-paths/**").permitAll()
                        // Hinglish: Certificate endpoints ko public kar diya
                        .requestMatchers("/api/certificates/**").permitAll()
                        // Hinglish: Swagger docs ke liye open access
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Hinglish: Root endpoint ko public kar diya testing ke liye
                        .requestMatchers("/", "/health", "/actuator/**").permitAll()
                        // Hinglish: baki sab authenticated users ke liye
                        .anyRequest().authenticated()
                )
                // Hinglish: session stateless kar diya kyunki JWT use kar rahe hai
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Hinglish: authentication provider aur JWT filter add kar diya
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Hinglish: DAO authentication provider setup kar rahe hai
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Hinglish: authentication manager bean banaya
        return config.getAuthenticationManager();
    }
}