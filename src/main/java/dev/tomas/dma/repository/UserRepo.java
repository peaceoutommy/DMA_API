package dev.tomas.dma.repository;

import dev.tomas.dma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByEmailContainingIgnoreCase(String emailPart);
}
