package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);

    Users findByUsername(String username);
}
