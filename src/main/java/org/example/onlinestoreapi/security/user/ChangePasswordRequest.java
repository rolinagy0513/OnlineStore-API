package org.example.onlinestoreapi.security.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing a password change request.
 *
 * <p>Contains the current password for verification, the new desired password,
 * and a confirmation of the new password to prevent typing errors.</p>
 *
 * Fields:
 * <ul>
 *   <li>{@code currentPassword} – the user's existing password.</li>
 *   <li>{@code newPassword} – the new password the user wants to set.</li>
 *   <li>{@code confirmationPassword} – a repeated entry of the new password for confirmation.</li>
 * </ul>
 */
@Getter
@Setter
@Builder
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
}
