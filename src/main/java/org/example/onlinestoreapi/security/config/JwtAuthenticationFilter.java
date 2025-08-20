package org.example.onlinestoreapi.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.security.token.TokenRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT Authentication Filter that intercepts incoming HTTP requests to:
 * <ul>
 *   <li>Extract and validate JWT tokens from httponly cookies.</li>
 *   <li>Authenticate users based on the extracted token.</li>
 *   <li>Enforce security by ensuring that protected endpoints require valid authentication.</li>
 *   <li>Bypass authentication for endpoints explicitly marked as "api/Auth".</li>
 * </ul>
 *
 * <p>This filter acts as a gatekeeper, allowing only authorized access to secured resources,
 * while permitting free access to designated public endpoints.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;
  private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    if (request.getServletPath().contains("/api/Auth")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwt = extractTokenFromCookies(request);

    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    final String userEmail = jwtService.extractUsername(jwt);

    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
      var isTokenValid = tokenRepository.findByToken(jwt)
              .map(t -> !t.isExpired() && !t.isRevoked())
              .orElse(false);

      if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
  }

  private String extractTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    return Arrays.stream(cookies)
            .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
  }
}