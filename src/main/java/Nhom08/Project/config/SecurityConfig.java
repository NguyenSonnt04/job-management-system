package Nhom08.Project.config;

import Nhom08.Project.handler.OAuth2LoginSuccessHandler;
import Nhom08.Project.service.CustomOAuth2UserService;
import Nhom08.Project.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // Enable CORS for API calls with credentials
            .csrf(csrf -> csrf.disable()) // Disable for development, enable in production
            .authorizeHttpRequests(auth -> auth
                // Public static resources
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .requestMatchers("/cv-files/**").permitAll()
                .requestMatchers("/includes/**").permitAll() // Header, footer includes
                .requestMatchers("/error").permitAll()
                .requestMatchers("/chatbot-ai/**").permitAll() // Chatbot AI files
                
                // Public pages (job listing, CV, etc.)
                .requestMatchers("/*.html").permitAll() // All root-level HTML pages
                .requestMatchers("/tim-viec-lam.html", "/nha-tuyen-dung.html").permitAll()
                .requestMatchers("/cham-diem-cv.html", "/tao-cv-ai.html", "/cv-editor.html").permitAll()
                
                // Authentication pages
                .requestMatchers("/auth/**", "/login", "/register/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/employer-login.html", "/employer-register.html", "/employer-register-step-2.html").permitAll()
                .requestMatchers("/candidate-login.html", "/candidate-register.html").permitAll()
                
                // API endpoints for registration and user info
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/user/**").permitAll()
                .requestMatchers("/api/announcements/active").permitAll() // Public announcements
                .requestMatchers("/api/chatbot/**").permitAll() // Chatbot API
                .requestMatchers("/api/contact-chat/**").permitAll() // Contact Widget Chatbot API
                .requestMatchers("/api/2fa/**").permitAll() // 2FA OTP verification

                // Admin pages and API - require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Employer pages - require EMPLOYER role  
                .requestMatchers("/employer/**").hasRole("EMPLOYER")
                .requestMatchers("/dashboard.html", "/quan-ly-dang-tuyen.html", "/quan-ly-ung-vien.html").hasRole("EMPLOYER")
                .requestMatchers("/quan-ly-dang-tuyen/**", "/post-job.html").hasRole("EMPLOYER")
                
                // Public Job API (read-only)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/jobs/active").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/jobs/search").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/jobs/{id}").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/uploads/image").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/uploads/file").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/uploads/images").authenticated()
                
                // Employer-only Job API (write operations)
                .requestMatchers("/api/jobs/create").hasRole("EMPLOYER")
                .requestMatchers("/api/jobs/employer-info").hasRole("EMPLOYER")
                .requestMatchers("/api/jobs/employer-logo").hasRole("EMPLOYER")
                .requestMatchers("/api/jobs/my-jobs").hasRole("EMPLOYER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/jobs/**").hasRole("EMPLOYER")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/jobs/**").hasRole("EMPLOYER")
                
                // User/Candidate pages - require USER role
                .requestMatchers("/profile/**", "/my-applications/**").hasRole("USER")
                
                // All other requests - permit for now (adjust as needed)
                .anyRequest().permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions
                    .sameOrigin() // Allow iframe from same origin
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/index.html")
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestURI = request.getRequestURI();
                    // Redirect employer pages to employer login
                    if (requestURI.contains("/dashboard") || 
                        requestURI.contains("/quan-ly") || 
                        requestURI.contains("/employer") ||
                        requestURI.contains("/post-job")) {
                        response.sendRedirect("/employer-login.html");
                    } else if (requestURI.contains("/admin")) {
                        response.sendRedirect("/login");
                    } else {
                        response.sendRedirect("/candidate-login.html");
                    }
                })
            );

        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/candidate-register.html?error=google")
            );
        }

        return http.build();
    }
}

