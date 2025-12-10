package com.yangshengzhou.gobang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/user/login")
                .ignoringRequestMatchers("/user/register")
                .ignoringRequestMatchers("/login")
                .ignoringRequestMatchers("/register")
                .csrfTokenRepository(csrfTokenRepository())
            )
            .authorizeHttpRequests(authz -> authz
                // 允许所有用户访问的公开接口 - 特别注意登录接口
                .requestMatchers("/user/login").permitAll()
                .requestMatchers("/user/register").permitAll()
                .requestMatchers("/user/info").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/findMatch").permitAll()
                .requestMatchers("/game").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/register").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 禁用默认的登录表单，因为使用JWT
            .formLogin(form -> form.disable())
            // 禁用HTTP Basic认证
            .httpBasic(basic -> basic.disable())
            // 禁用session管理，因为使用JWT
            .sessionManagement(session -> session.disable())
            // 禁用匿名用户
            .anonymous(anonymous -> anonymous.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new CookieCsrfTokenRepository();
    }
}