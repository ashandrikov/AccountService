package org.shandrikov.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests() // manage access
                    .mvcMatchers(HttpMethod.POST, "api/auth/signup").permitAll()
                    .mvcMatchers(HttpMethod.POST, "api/auth/changepass").hasAnyAuthority("USER", "ACCOUNTANT", "ADMINISTRATOR")
                    .mvcMatchers(HttpMethod.GET, "api/empl/payment").hasAnyAuthority("USER", "ACCOUNTANT", "ADMINISTRATOR")
                    .mvcMatchers("api/acct/**").hasAnyAuthority("ACCOUNTANT", "ADMINISTRATOR")
                    .mvcMatchers("api/admin/**").hasAuthority("ADMINISTRATOR")
                    .mvcMatchers("api/security/**").hasAnyAuthority("AUDITOR", "ADMINISTRATOR")
                    .anyRequest().permitAll()
                .and()
                    .httpBasic()
                    .authenticationEntryPoint(new RestAuthenticationEntryPoint()) // Handle auth error
                .and()
                    .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                .and()
                    .exceptionHandling().accessDeniedHandler(accessDeniedHandler());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(getEncoder());
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
}
