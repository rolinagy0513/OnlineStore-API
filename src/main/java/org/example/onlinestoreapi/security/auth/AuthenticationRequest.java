package org.example.onlinestoreapi.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing the authentication request payload.
 * <p>
 * Used for submitting user credentials during the authentication process.
 * Typically consumed by the {@code /authenticate} endpoint.
 * </p>
 *
 * <p>
 * Contains:
 * <ul>
 *   <li>{@code email} – The user's email address.</li>
 *   <li>{@code password} – The user's password.</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

  private String email;
  String password;
}
