package org.example.onlinestoreapi.security.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing the authentication response payload.
 * <p>
 * Returned by authentication-related endpoints (e.g., {@code /register}, {@code /authenticate}, {@code /refresh-token}).
 * </p>
 *
 * <p>
 * Contains:
 * <ul>
 *   <li>{@code message} – A status or informational message about the authentication result.</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

  private String message;

}
