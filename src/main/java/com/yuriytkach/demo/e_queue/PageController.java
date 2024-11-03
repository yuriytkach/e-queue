package com.yuriytkach.demo.e_queue;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PageController {

    @GetMapping(value = "/", produces = "text/html")
    public ModelAndView get() {
        return new ModelAndView("home", Map.of());
    }

    @GetMapping("/user")
    public Map<String, Object> user(final @AuthenticationPrincipal OAuth2User principal) {
      final String name = principal.getAttribute("name") == null ? "Unknown" : principal.getAttribute("name");
      return Map.of("name", name);
    }
}
