package com.sarvasvanaturals.config;

import com.sarvasvanaturals.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/shop/**", "/product/**", "/category/**",
                                 "/about", "/our-process", "/lab-reports", "/contact",
                                 "/search", "/verify-purity", "/batch/**", "/refund-policy", "/privacy-policy", "/shipping-info").permitAll()
                // Auth pages
                .requestMatchers("/login", "/register", "/forgot-password",
                                 "/reset-password/**", "/verify-email/**").permitAll()
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**",
                                 "/fonts/**", "/favicon.ico").permitAll()
                // Payment webhooks (no auth, signature verified)
                .requestMatchers("/webhook/**").permitAll()
                // H2 console (dev only)
                .requestMatchers("/h2-console/**").permitAll()
                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // API
                .requestMatchers("/api/cart/**").permitAll()
                // Everything else needs auth
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    response.sendRedirect(isAdmin ? "/admin" : "/");
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "cart_session")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("sarvasva-naturals-remember-me-key")
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 days
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/webhook/**", "/api/**", "/h2-console/**")
            )
            // Allow H2 console frames
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }
}
