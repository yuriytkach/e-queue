package com.yuriytkach.demo.e_queue;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Authentication authentication) throws IOException {

    final OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

    final String id = oauthUser.getAttribute("id").toString();
    final String name = oauthUser.getAttribute("name") == null ? "Unknown" : oauthUser.getAttribute("name").toString();

    log.info("Authenticated oAuth2 user id: {}", id);

    final User user = new User(id, name);
    userRepository.save(user);

    response.sendRedirect("/");
  }
}
