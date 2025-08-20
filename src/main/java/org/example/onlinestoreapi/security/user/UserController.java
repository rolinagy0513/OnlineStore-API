package org.example.onlinestoreapi.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller managing user-related operations such as password changes
 * and retrieving user information.
 * <p>
 * Provides endpoints to change the authenticated user's password, fetch all users
 * except a specified one, and retrieve details of the currently authenticated user.
 * </p>
 */
@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param request the password change request containing current, new, and confirmation passwords
     * @param connectedUser the security principal of the authenticated user
     * @return HTTP 200 OK on successful password change
     */
    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(
          @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

}
