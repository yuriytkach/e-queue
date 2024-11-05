package com.yuriytkach.demo.e_queue.config;

import com.yuriytkach.demo.e_queue.service.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final CustomAuthenticationSuccessHandler successHandler;

  @Bean
  @Order(1)
  SecurityFilterChain securityFilterChain1(final HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests((requests) -> requests
        .requestMatchers(
          new AntPathRequestMatcher("/"),
          new AntPathRequestMatcher("/error"),
          new AntPathRequestMatcher("/webjars/**"),
          new AntPathRequestMatcher("/actuator/**")
        ).permitAll()
        .anyRequest().authenticated()
      )
      .logout(customizer -> customizer.logoutSuccessUrl("/").permitAll())
      .exceptionHandling(customizer -> customizer
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
      )
      .oauth2Login(customizer -> customizer.successHandler(successHandler));

    return http.build();
  }

}
