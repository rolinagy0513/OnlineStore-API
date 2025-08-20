package org.example.onlinestoreapi.security.auditing;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.onlinestoreapi.security.user.User;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Provides the current authenticated user's ID for auditing purposes.
 * <p>
 * Used by Spring Data JPA auditing annotations such as {@code @CreatedBy} and
 * {@code @LastModifiedBy} to automatically populate entity fields.
 * The auditor information is retrieved from the {@link SecurityContextHolder}.
 * </p>
 *
 * <p>
 * Requires {@code @EnableJpaAuditing(auditorAwareRef = "auditorAware")} to be
 * configured in the application.
 * </p>
 */
public class ApplicationAuditAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.empty();
        }

        User userPrincipal = (User) authentication.getPrincipal();
        return Optional.ofNullable(userPrincipal.getId());
    }
}