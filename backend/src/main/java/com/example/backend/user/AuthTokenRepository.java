package com.example.backend.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    @EntityGraph(attributePaths = "user")
    Optional<AuthToken> findWithUserByToken(String token);
}
