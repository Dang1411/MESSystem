package com.mes.config;

import com.mes.security.JwtAuthenticationFilter;
import com.mes.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình Spring Security với JWT
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/api/auth/**").permitAll()
                // Swagger (nếu có)
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // SUPERVISOR only
                .antMatchers(HttpMethod.POST, "/api/users/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/api/users/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.POST, "/api/products/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.POST, "/api/process-steps/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/api/process-steps/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.DELETE, "/api/process-steps/**").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.POST, "/api/production-orders").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/api/production-orders/**").hasRole("SUPERVISOR")
                //.antMatchers("/api/dashboard/**").hasRole("SUPERVISOR")
                .antMatchers("/api/dashboard/**").hasAnyRole("SUPERVISOR", "OPERATOR", "QC")
                .antMatchers("/api/reports/**").hasRole("SUPERVISOR")
                .antMatchers("/api/traceability/**").hasAnyRole("SUPERVISOR", "QC")
                // Tất cả API còn lại yêu cầu xác thực
                .anyRequest().authenticated();

        // Thêm JWT filter trước filter xác thực username/password
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
