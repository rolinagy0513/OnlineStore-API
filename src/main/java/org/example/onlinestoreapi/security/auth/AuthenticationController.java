package org.example.onlinestoreapi.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST controller for handling authentication-related operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>User registration</li>
 *   <li>User authentication (login)</li>
 *   <li>JWT refresh token generation</li>
 * </ul>
 * All responses return an {@link AuthenticationResponse} containing authentication
 * details such as tokens.
 * </p>
 *
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code POST /api/Auth/register} – Registers a new user.</li>
 *   <li>{@code POST /api/Auth/authenticate} – Authenticates an existing user.</li>
 *   <li>{@code POST /api/Auth/refresh-token} – Refreshes the JWT access token.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/Auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
          @Valid @RequestBody RegisterRequest request,
          HttpServletResponse response
  ) {
    return ResponseEntity.ok(service.register(request, response));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
          @RequestBody AuthenticationRequest request,
          HttpServletResponse response
  ) {
    return ResponseEntity.ok(service.authenticate(request, response));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthenticationResponse> refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    ResponseEntity<AuthenticationResponse> authResponse = service.refreshToken(request, response);
    return authResponse;
  }

}