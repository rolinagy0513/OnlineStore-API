package org.example.onlinestoreapi.security.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing the registration request payload.
 * <p>
 * Used by the {@code /register} authentication endpoint to capture the details
 * required for creating a new user account.
 * </p>
 *
 * <p>
 * Contains:
 * <ul>
 *   <li>{@code firstname} – The user's first name (required).</li>
 *   <li>{@code lastname} – The user's last name (required).</li>
 *   <li>{@code email} – The user's email address (required).</li>
 *   <li>{@code password} – The user's chosen password (required).</li>
 *   <li>{@code confirmPassword} – Confirmation of the chosen password (required).</li>
 * </ul>
 * </p>
 *
 * Validation annotations ensure that none of these fields are blank.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Firstname is required")
  private String firstname;
  @NotBlank(message = "Lastname is required" )
  private String lastname;
  @NotBlank(message = "Email is required")
  private String email;
  @NotBlank(message = "Password is required")
  private String password;
}
