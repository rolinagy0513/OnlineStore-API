package org.example.onlinestoreapi.security.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.exception.UserNotFoundException;
import org.example.onlinestoreapi.security.config.JwtService;
import org.example.onlinestoreapi.security.token.Token;
import org.example.onlinestoreapi.security.token.TokenRepository;
import org.example.onlinestoreapi.security.token.TokenType;
import org.example.onlinestoreapi.security.user.User;
import org.example.onlinestoreapi.security.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/**
 * Service class responsible for handling authentication-related operations such as
 * user registration, login, token generation, and token refresh.
 * <p>
 * Integrates with Spring Security for authentication and uses JWT for stateless session management.
 * Stores tokens in cookies for client-side usage.
 * </p>
 */@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;

  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration;

  private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  /**
   * Registers a new user, validates the password, stores the user in the database,
   * generates JWT access and refresh tokens, stores them, and sets them in HTTP cookies.
   *
   * @param request  The registration request containing user details and passwords.
   * @param response The HTTP response to which cookies will be added.
   * @return An {@link AuthenticationResponse} containing a success message.
   */
  public AuthenticationResponse register(RegisterRequest request, HttpServletResponse response) {
    var user = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
    var savedUser = repository.save(user);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);

    addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, jwtToken, (int) (jwtExpiration / 1000));
    addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) (refreshExpiration / 1000));

    return AuthenticationResponse.builder()
            .message("Registration complete")
            .build();
  }

  /**
   * Authenticates a user using the provided credentials, generates new JWT access and refresh tokens,
   * revokes previous tokens, stores the new ones, and sets them in HTTP cookies.
   *
   * @param request  The authentication request containing email and password.
   * @param response The HTTP response to which cookies will be added.
   * @return An {@link AuthenticationResponse} containing a success message.
   */
  public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
    );
    var user = repository.findByEmail(request.getEmail())
            .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, jwtToken, (int) (jwtExpiration / 1000));
    addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) (refreshExpiration / 1000));

    return AuthenticationResponse.builder()
            .message("Login complete")
            .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  /**
   * Refreshes the access token using a valid refresh token stored in the client's cookies.
   * If valid, issues a new access token and updates cookies.
   *
   * @param request  The HTTP request containing cookies.
   * @param response The HTTP response to which the new access token cookie will be added.
   * @return A {@link ResponseEntity} containing an {@link AuthenticationResponse} message.
   * @throws IOException If an I/O error occurs while processing the request.
   */
  public ResponseEntity<AuthenticationResponse> refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(AuthenticationResponse.builder()
                      .message("Cookie not found")
                      .build()
              );
    }

    String refreshToken = Arrays.stream(cookies)
            .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(AuthenticationResponse.builder()
                      .message("Refresh token was not found")
                      .build()
              );
    }

    final String userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = repository.findByEmail(userEmail)
              .orElseThrow(() -> new UserNotFoundException(userEmail));
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) (jwtExpiration / 1000));

       return ResponseEntity.ok()
               .body(AuthenticationResponse.builder()
                       .message("New token was given")
                       .build()
               );
      }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(AuthenticationResponse.builder()
                    .message("Invalid refresh Token")
                    .build()
            );
  }

  private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  /**
   * Retrieves the currently authenticated user from the security context.
   *
   * @return The authenticated {@link User}.
   * @throws IllegalStateException   if no authenticated user is found or the authentication principal is invalid.
   * @throws UserNotFoundException   if the user is not found in the database.
   */
  public User getAuthenticatedUser(){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ){
      throw new IllegalStateException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();
    String email;

    if(principal instanceof UserDetails){
      email = ((UserDetails)principal).getUsername();
    }else{
      throw new IllegalStateException("Unexpected authentication principal");
    }

    return repository.findByEmail(email)
            .orElseThrow(()->new UserNotFoundException(email));
  }

}