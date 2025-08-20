package org.example.onlinestoreapi.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing User entities.
 * Provides methods to search the user by email, ID.
 * And a flexible search query that can retrieve users from their usernames
 */
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  User findById(Long id);

}
