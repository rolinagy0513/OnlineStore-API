package org.example.onlinestoreapi.security.user;

import lombok.RequiredArgsConstructor;
import org.example.onlinestoreapi.exception.UserNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * Service class responsible for user-related business logic such as
 * retrieving the current authenticated user, fetching other users,
 * and handling password changes.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    /**
     * Retrieves the currently authenticated user's basic information.
     *
     * @return a UserDTO containing the authenticated user's ID and full name
     * @throws UserNotFoundException if no user matches the authenticated email
     */
    public User getCurrentUser( ){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(email));
    }

    public Long getCurrentUserId(){
        return getCurrentUser().getId();
    }

    /**
     * Changes the password of the authenticated user after validating the current password
     * and confirming that the new password matches the confirmation.
     *
     * @param request the password change request containing current, new, and confirmation passwords
     * @param connectedUser the authenticated user's Principal
     * @throws IllegalStateException if the current password is incorrect or the new passwords do not match
     */
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        repository.save(user);
    }
}
