package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.Owners;

public interface OwnersRepository extends JpaRepository<Owners, Long> {

    // Custom query to find an owner by email (optional)
    Owners findByEmail(String email);
}
