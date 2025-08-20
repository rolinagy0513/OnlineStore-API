package org.example.onlinestoreapi.security.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.security.token.TokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * A service that handles the logout operations such as:
 * <ul>
 *     <li>Extracting the token from the httponly cookies</li>
 *     <li>Changing their status to REVOKED and EXPIRED in the db</li>
 *     <li>And clears the httponly cookies based on the name</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

  private final TokenRepository tokenRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String jwt = extractTokenFromCookies(request);
    if (jwt == null) {
      return;
    }

    var storedToken = tokenRepository.findByToken(jwt).orElse(null);
    if (storedToken != null) {
      storedToken.setExpired(true);
      storedToken.setRevoked(true);
      tokenRepository.save(storedToken);
    }

    clearCookie(response, "access_token");
    clearCookie(response, "refresh_token");

    SecurityContextHolder.clearContext();
  }

  private String extractTokenFromCookies(HttpServletRequest request) {
    if (request.getCookies() == null) return null;

    return Arrays.stream(request.getCookies())
            .filter(cookie -> "access_token".equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
  }

  private void clearCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}
